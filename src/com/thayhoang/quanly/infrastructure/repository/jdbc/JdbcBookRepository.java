package com.thayhoang.quanly.infrastructure.repository.jdbc;

import com.thayhoang.quanly.application.repository.BookRepository;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.infrastructure.db.DatabaseSupport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcBookRepository implements BookRepository {
    @Override
    public Optional<Book> findById(String bookId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection()) {
            return findById(connection, bookId);
        }
    }

    @Override
    public Optional<Book> findById(Connection connection, String bookId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT book_id, title, author, publisher, year_publish, quantity_total, quantity_available, status
                FROM BOOK
                WHERE book_id = ?
                """)) {
            statement.setString(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Book> findAll() throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT book_id, title, author, publisher, year_publish, quantity_total, quantity_available, status
                        FROM BOOK
                        ORDER BY title
                        """);
                ResultSet resultSet = statement.executeQuery()) {
            return mapRows(resultSet);
        }
    }

    @Override
    public List<Book> search(String keyword, boolean onlyAvailable) throws SQLException {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        String likeKeyword = "%" + normalizedKeyword + "%";

        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT book_id, title, author, publisher, year_publish, quantity_total, quantity_available, status
                        FROM BOOK
                        WHERE (
                            ? = ''
                            OR lower(book_id) LIKE ?
                            OR lower(title) LIKE ?
                            OR lower(author) LIKE ?
                        )
                        AND (? = 0 OR quantity_available > 0)
                        ORDER BY title
                        """)) {
            statement.setString(1, normalizedKeyword);
            statement.setString(2, likeKeyword);
            statement.setString(3, likeKeyword);
            statement.setString(4, likeKeyword);
            statement.setInt(5, onlyAvailable ? 1 : 0);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        }
    }

    @Override
    public Book save(Book book) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection()) {
            return save(connection, book);
        }
    }

    @Override
    public Book save(Connection connection, Book book) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO BOOK (
                    book_id, title, author, publisher, year_publish, quantity_total, quantity_available, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            bind(statement, book, false);
            statement.executeUpdate();
            return book;
        }
    }

    @Override
    public Book update(Book book) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection()) {
            return update(connection, book);
        }
    }

    @Override
    public Book update(Connection connection, Book book) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE BOOK
                SET title = ?, author = ?, publisher = ?, year_publish = ?, quantity_total = ?, quantity_available = ?, status = ?
                WHERE book_id = ?
                """)) {
            bind(statement, book, true);
            statement.executeUpdate();
            return book;
        }
    }

    @Override
    public int count() throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM BOOK");
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private void bind(PreparedStatement statement, Book book, boolean forUpdate) throws SQLException {
        int index = 1;
        if (!forUpdate) {
            statement.setString(index++, book.bookId());
        }
        statement.setString(index++, book.title());
        statement.setString(index++, book.author());
        statement.setString(index++, book.publisher());
        statement.setInt(index++, book.yearPublish());
        statement.setInt(index++, book.quantityTotal());
        statement.setInt(index++, book.quantityAvailable());
        statement.setString(index++, book.status().name());
        if (forUpdate) {
            statement.setString(index, book.bookId());
        }
    }

    private List<Book> mapRows(ResultSet resultSet) throws SQLException {
        List<Book> books = new ArrayList<>();
        while (resultSet.next()) {
            books.add(mapRow(resultSet));
        }
        return List.copyOf(books);
    }

    private Book mapRow(ResultSet resultSet) throws SQLException {
        return new Book(
                resultSet.getString("book_id"),
                resultSet.getString("title"),
                resultSet.getString("author"),
                resultSet.getString("publisher"),
                resultSet.getInt("year_publish"),
                resultSet.getInt("quantity_total"),
                resultSet.getInt("quantity_available"),
                BookStatus.valueOf(resultSet.getString("status")));
    }
}
