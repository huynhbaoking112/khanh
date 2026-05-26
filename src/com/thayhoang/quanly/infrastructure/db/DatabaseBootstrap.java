package com.thayhoang.quanly.infrastructure.db;

import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.enums.UserRole;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseBootstrap {
    private DatabaseBootstrap() {
    }

    public static void initialize() throws SQLException {
        ensureDataDirectory();
        ensureDriver();

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.jdbcUrl());
                Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            createSchema(statement);
            seedData(connection);
        }
    }

    private static void createSchema(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS LIBRARIAN (
                    librarian_id TEXT PRIMARY KEY,
                    full_name TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS READER (
                    reader_id TEXT PRIMARY KEY,
                    full_name TEXT NOT NULL,
                    phone TEXT,
                    email TEXT,
                    max_borrow INTEGER NOT NULL DEFAULT 3 CHECK (max_borrow >= 0),
                    status TEXT NOT NULL
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS BOOK (
                    book_id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    publisher TEXT,
                    year_publish INTEGER NOT NULL,
                    quantity_total INTEGER NOT NULL CHECK (quantity_total >= 0),
                    quantity_available INTEGER NOT NULL CHECK (
                        quantity_available >= 0 AND quantity_available <= quantity_total
                    ),
                    status TEXT NOT NULL
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS LOAN (
                    loan_id TEXT PRIMARY KEY,
                    reader_id TEXT NOT NULL,
                    librarian_id TEXT NOT NULL,
                    loan_date TEXT NOT NULL,
                    due_date TEXT NOT NULL,
                    return_date TEXT,
                    status TEXT NOT NULL,
                    FOREIGN KEY (reader_id) REFERENCES READER(reader_id),
                    FOREIGN KEY (librarian_id) REFERENCES LIBRARIAN(librarian_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS LOAN_DETAIL (
                    loan_detail_id TEXT PRIMARY KEY,
                    loan_id TEXT NOT NULL,
                    book_id TEXT NOT NULL,
                    quantity INTEGER NOT NULL CHECK (quantity > 0),
                    returned INTEGER NOT NULL DEFAULT 0,
                    note TEXT,
                    UNIQUE (loan_id, book_id),
                    FOREIGN KEY (loan_id) REFERENCES LOAN(loan_id) ON DELETE CASCADE,
                    FOREIGN KEY (book_id) REFERENCES BOOK(book_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS FINE (
                    fine_id TEXT PRIMARY KEY,
                    loan_id TEXT NOT NULL UNIQUE,
                    amount NUMERIC NOT NULL,
                    reason TEXT NOT NULL,
                    paid_status TEXT NOT NULL,
                    FOREIGN KEY (loan_id) REFERENCES LOAN(loan_id) ON DELETE CASCADE
                )
                """);
    }

    private static void ensureDataDirectory() throws SQLException {
        try {
            Files.createDirectories(DatabaseConfig.dataDirectory());
        } catch (IOException exception) {
            throw new SQLException("Khong the tao thu muc du lieu SQLite", exception);
        }
    }

    private static void ensureDriver() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("Khong tim thay SQLite JDBC driver trong thu muc lib", exception);
        }
    }

    private static void seedData(Connection connection) throws SQLException {
        seedLibrarians(connection);
        seedReaders(connection);
        seedBooks(connection);
    }

    private static void seedLibrarians(Connection connection) throws SQLException {
        if (countRows(connection, "LIBRARIAN") > 0) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO LIBRARIAN (librarian_id, full_name, username, password, role)
                VALUES (?, ?, ?, ?, ?)
                """)) {
            insertLibrarian(statement, "LIB001", "System Administrator", "admin", "admin123", UserRole.ADMIN);
            insertLibrarian(statement, "LIB002", "Thu Thu Mac Dinh", "librarian", "lib123", UserRole.LIBRARIAN);
        }
    }

    private static void insertLibrarian(
            PreparedStatement statement,
            String id,
            String fullName,
            String username,
            String password,
            UserRole role) throws SQLException {
        statement.setString(1, id);
        statement.setString(2, fullName);
        statement.setString(3, username);
        statement.setString(4, password);
        statement.setString(5, role.name());
        statement.executeUpdate();
    }

    private static void seedReaders(Connection connection) throws SQLException {
        if (countRows(connection, "READER") > 0) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO READER (reader_id, full_name, phone, email, max_borrow, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            insertReader(statement, "R001", "Nguyen Van An", "0901000001", "an@example.com", 3, ReaderStatus.ACTIVE);
            insertReader(statement, "R002", "Tran Thi Binh", "0901000002", "binh@example.com", 3, ReaderStatus.ACTIVE);
            insertReader(statement, "R003", "Le Minh Khoa", "0901000003", "khoa@example.com", 3, ReaderStatus.INACTIVE);
        }
    }

    private static void insertReader(
            PreparedStatement statement,
            String id,
            String fullName,
            String phone,
            String email,
            int maxBorrow,
            ReaderStatus status) throws SQLException {
        statement.setString(1, id);
        statement.setString(2, fullName);
        statement.setString(3, phone);
        statement.setString(4, email);
        statement.setInt(5, maxBorrow);
        statement.setString(6, status.name());
        statement.executeUpdate();
    }

    private static void seedBooks(Connection connection) throws SQLException {
        if (countRows(connection, "BOOK") > 0) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO BOOK (
                    book_id, title, author, publisher, year_publish, quantity_total, quantity_available, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            insertBook(statement, "B001", "Lap Trinh Java Co Ban", "Nguyen Huu Lap", "Giao Duc", 2022, 5, 5, BookStatus.AVAILABLE);
            insertBook(statement, "B002", "Cau Truc Du Lieu", "Tran Van Tri", "Thong Tin", 2021, 3, 2, BookStatus.AVAILABLE);
            insertBook(statement, "B003", "Phan Tich He Thong", "Le Hoang", "Dai Hoc Quoc Gia", 2020, 2, 0, BookStatus.AVAILABLE);
        }
    }

    private static void insertBook(
            PreparedStatement statement,
            String id,
            String title,
            String author,
            String publisher,
            int yearPublish,
            int quantityTotal,
            int quantityAvailable,
            BookStatus status) throws SQLException {
        statement.setString(1, id);
        statement.setString(2, title);
        statement.setString(3, author);
        statement.setString(4, publisher);
        statement.setInt(5, yearPublish);
        statement.setInt(6, quantityTotal);
        statement.setInt(7, quantityAvailable);
        statement.setString(8, status.name());
        statement.executeUpdate();
    }

    private static int countRows(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }
}
