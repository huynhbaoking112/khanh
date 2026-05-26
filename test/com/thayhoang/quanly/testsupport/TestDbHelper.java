package com.thayhoang.quanly.testsupport;

import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.enums.UserRole;
import com.thayhoang.quanly.infrastructure.db.DatabaseBootstrap;
import com.thayhoang.quanly.infrastructure.db.DatabaseSupport;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TestDbHelper {
    private TestDbHelper() {
    }

    public static void initSchema() throws SQLException {
        DatabaseBootstrap.initialize();
    }

    public static void deleteLibrarian(String librarianId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM LIBRARIAN WHERE librarian_id = ?")) {
            statement.setString(1, librarianId);
            statement.executeUpdate();
        }
    }

    public static void upsertLibrarian(String librarianId, String fullName, String username, String password, UserRole role)
            throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO LIBRARIAN (librarian_id, full_name, username, password, role) "
                                + "VALUES (?, ?, ?, ?, ?) "
                                + "ON CONFLICT(librarian_id) DO UPDATE SET "
                                + "full_name=excluded.full_name, username=excluded.username, "
                                + "password=excluded.password, role=excluded.role")) {
            statement.setString(1, librarianId);
            statement.setString(2, fullName);
            statement.setString(3, username);
            statement.setString(4, password);
            statement.setString(5, role.name());
            statement.executeUpdate();
        }
    }

    public static String dumpLibrarian(String librarianId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT librarian_id, full_name, username, role FROM LIBRARIAN WHERE librarian_id = ?")) {
            statement.setString(1, librarianId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return "(not present)";
                }
                return String.format("id=%s | name=%s | username=%s | role=%s",
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        }
    }

    public static void deleteReader(String readerId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM READER WHERE reader_id = ?")) {
            statement.setString(1, readerId);
            statement.executeUpdate();
        }
    }

    public static void deleteLoansForReader(String readerId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM LOAN WHERE reader_id = ?")) {
            statement.setString(1, readerId);
            statement.executeUpdate();
        }
    }

    public static void upsertReader(
            String readerId, String fullName, String phone, String email, int maxBorrow, ReaderStatus status)
            throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO READER (reader_id, full_name, phone, email, max_borrow, status) "
                                + "VALUES (?,?,?,?,?,?) "
                                + "ON CONFLICT(reader_id) DO UPDATE SET "
                                + "full_name=excluded.full_name, phone=excluded.phone, email=excluded.email, "
                                + "max_borrow=excluded.max_borrow, status=excluded.status")) {
            statement.setString(1, readerId);
            statement.setString(2, fullName);
            statement.setString(3, phone);
            statement.setString(4, email);
            statement.setInt(5, maxBorrow);
            statement.setString(6, status.name());
            statement.executeUpdate();
        }
    }

    public static String dumpReader(String readerId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT reader_id, full_name, phone, email, max_borrow, status FROM READER WHERE reader_id = ?")) {
            statement.setString(1, readerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return "(not present)";
                }
                return String.format(
                        "id=%s | name=%s | phone='%s' | email=%s | max=%d | status=%s",
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getInt(5), rs.getString(6));
            }
        }
    }

    public static void deleteBook(String bookId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM BOOK WHERE book_id = ?")) {
            statement.setString(1, bookId);
            statement.executeUpdate();
        }
    }

    public static void upsertBook(
            String bookId,
            String title,
            String author,
            String publisher,
            int yearPublish,
            int quantityTotal,
            int quantityAvailable,
            BookStatus status) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO BOOK (book_id, title, author, publisher, year_publish, quantity_total, quantity_available, status) "
                                + "VALUES (?,?,?,?,?,?,?,?) "
                                + "ON CONFLICT(book_id) DO UPDATE SET "
                                + "title=excluded.title, author=excluded.author, publisher=excluded.publisher, "
                                + "year_publish=excluded.year_publish, quantity_total=excluded.quantity_total, "
                                + "quantity_available=excluded.quantity_available, status=excluded.status")) {
            statement.setString(1, bookId);
            statement.setString(2, title);
            statement.setString(3, author);
            statement.setString(4, publisher);
            statement.setInt(5, yearPublish);
            statement.setInt(6, quantityTotal);
            statement.setInt(7, quantityAvailable);
            statement.setString(8, status.name());
            statement.executeUpdate();
        }
    }

    public static String dumpBook(String bookId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT book_id, title, author, year_publish, quantity_total, quantity_available, status FROM BOOK WHERE book_id = ?")) {
            statement.setString(1, bookId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return "(not present)";
                }
                return String.format(
                        "id=%s | title=%s | author=%s | year=%d | total=%d | available=%d | status=%s",
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5),
                        rs.getInt(6), rs.getString(7));
            }
        }
    }

    public static void deleteLoan(String loanId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM LOAN WHERE loan_id = ?")) {
            statement.setString(1, loanId);
            statement.executeUpdate();
        }
    }

    public static void upsertLoan(
            String loanId,
            String readerId,
            String librarianId,
            LocalDate loanDate,
            LocalDate dueDate,
            LocalDate returnDate,
            LoanStatus status) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO LOAN (loan_id, reader_id, librarian_id, loan_date, due_date, return_date, status) "
                                + "VALUES (?,?,?,?,?,?,?) "
                                + "ON CONFLICT(loan_id) DO UPDATE SET "
                                + "reader_id=excluded.reader_id, librarian_id=excluded.librarian_id, "
                                + "loan_date=excluded.loan_date, due_date=excluded.due_date, "
                                + "return_date=excluded.return_date, status=excluded.status")) {
            statement.setString(1, loanId);
            statement.setString(2, readerId);
            statement.setString(3, librarianId);
            statement.setString(4, loanDate.toString());
            statement.setString(5, dueDate.toString());
            statement.setString(6, returnDate == null ? null : returnDate.toString());
            statement.setString(7, status.name());
            statement.executeUpdate();
        }
    }

    public static String dumpLoan(String loanId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT loan_id, reader_id, librarian_id, loan_date, due_date, return_date, status FROM LOAN WHERE loan_id = ?")) {
            statement.setString(1, loanId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return "(not present)";
                }
                return String.format(
                        "loan=%s | reader=%s | librarian=%s | loan_date=%s | due=%s | return=%s | status=%s",
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                        rs.getString(6), rs.getString(7));
            }
        }
    }

    public static void upsertLoanDetail(
            String loanDetailId,
            String loanId,
            String bookId,
            int quantity,
            boolean returned,
            String note) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO LOAN_DETAIL (loan_detail_id, loan_id, book_id, quantity, returned, note) "
                                + "VALUES (?, ?, ?, ?, ?, ?) "
                                + "ON CONFLICT(loan_detail_id) DO UPDATE SET "
                                + "loan_id=excluded.loan_id, book_id=excluded.book_id, quantity=excluded.quantity, "
                                + "returned=excluded.returned, note=excluded.note")) {
            statement.setString(1, loanDetailId);
            statement.setString(2, loanId);
            statement.setString(3, bookId);
            statement.setInt(4, quantity);
            statement.setInt(5, returned ? 1 : 0);
            statement.setString(6, note);
            statement.executeUpdate();
        }
    }

    public static String dumpLoanDetail(String loanDetailId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT loan_detail_id, loan_id, book_id, quantity, returned, note FROM LOAN_DETAIL WHERE loan_detail_id = ?")) {
            statement.setString(1, loanDetailId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return "(not present)";
                }
                return String.format(
                        "id=%s | loan=%s | book=%s | qty=%d | returned=%s | note=%s",
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4),
                        rs.getInt(5) == 1, rs.getString(6));
            }
        }
    }

    public static void upsertFine(
            String fineId,
            String loanId,
            BigDecimal amount,
            String reason,
            FinePaymentStatus status) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO FINE (fine_id, loan_id, amount, reason, paid_status) VALUES (?, ?, ?, ?, ?) "
                                + "ON CONFLICT(loan_id) DO UPDATE SET amount=excluded.amount, reason=excluded.reason, paid_status=excluded.paid_status")) {
            statement.setString(1, fineId);
            statement.setString(2, loanId);
            statement.setBigDecimal(3, amount);
            statement.setString(4, reason);
            statement.setString(5, status.name());
            statement.executeUpdate();
        }
    }

    public static String dumpFine(String loanId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT fine_id, loan_id, amount, reason, paid_status FROM FINE WHERE loan_id = ?")) {
            statement.setString(1, loanId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return "(not present)";
                }
                return String.format("id=%s | loan=%s | amount=%s | reason=%s | status=%s",
                        rs.getString(1), rs.getString(2), rs.getBigDecimal(3), rs.getString(4), rs.getString(5));
            }
        }
    }

    public static int countRows(String tableName) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    public static List<String> findLoanIdsForReader(String readerId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT loan_id FROM LOAN WHERE reader_id = ? ORDER BY loan_id")) {
            statement.setString(1, readerId);
            try (ResultSet rs = statement.executeQuery()) {
                List<String> loanIds = new ArrayList<>();
                while (rs.next()) {
                    loanIds.add(rs.getString(1));
                }
                return List.copyOf(loanIds);
            }
        }
    }
}