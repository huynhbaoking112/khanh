package com.thayhoang.quanly.khoa_reader;

import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.infrastructure.db.DatabaseBootstrap;
import com.thayhoang.quanly.infrastructure.db.DatabaseSupport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

// Test helper that lets Khoa's tests hit the real SQLite DB (data/library.db).
// Each test uses these helpers to set up known state, then read back rows for the QcReport.
public final class TestDbHelper {
    private TestDbHelper() {
    }

    public static void initSchema() throws SQLException {
        DatabaseBootstrap.initialize();
    }

    // --- READER helpers ---

    public static void deleteReader(String readerId) throws SQLException {
        try (Connection conn = DatabaseSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM READER WHERE reader_id = ?")) {
            stmt.setString(1, readerId);
            stmt.executeUpdate();
        }
    }

    public static void upsertReader(
            String readerId, String fullName, String phone, String email, int maxBorrow, ReaderStatus status)
            throws SQLException {
        // Use SQLite ON CONFLICT to avoid DELETE (which would trigger FK cascade on existing LOAN rows).
        try (Connection conn = DatabaseSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO READER (reader_id, full_name, phone, email, max_borrow, status) "
                                + "VALUES (?,?,?,?,?,?) "
                                + "ON CONFLICT(reader_id) DO UPDATE SET "
                                + "full_name=excluded.full_name, "
                                + "phone=excluded.phone, "
                                + "email=excluded.email, "
                                + "max_borrow=excluded.max_borrow, "
                                + "status=excluded.status")) {
            stmt.setString(1, readerId);
            stmt.setString(2, fullName);
            stmt.setString(3, phone);
            stmt.setString(4, email);
            stmt.setInt(5, maxBorrow);
            stmt.setString(6, status.name());
            stmt.executeUpdate();
        }
    }

    public static String dumpReader(String readerId) throws SQLException {
        try (Connection conn = DatabaseSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT reader_id, full_name, phone, email, max_borrow, status FROM READER WHERE reader_id = ?")) {
            stmt.setString(1, readerId);
            try (ResultSet rs = stmt.executeQuery()) {
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

    public static boolean readerExists(String readerId) throws SQLException {
        try (Connection conn = DatabaseSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM READER WHERE reader_id = ?")) {
            stmt.setString(1, readerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // --- LOAN helpers ---

    public static void deleteLoan(String loanId) throws SQLException {
        try (Connection conn = DatabaseSupport.getConnection()) {
            try (PreparedStatement delDetail = conn.prepareStatement("DELETE FROM LOAN_DETAIL WHERE loan_id = ?")) {
                delDetail.setString(1, loanId);
                delDetail.executeUpdate();
            }
            try (PreparedStatement delLoan = conn.prepareStatement("DELETE FROM LOAN WHERE loan_id = ?")) {
                delLoan.setString(1, loanId);
                delLoan.executeUpdate();
            }
        }
    }

    public static void upsertLoan(
            String loanId, String readerId, String librarianId,
            LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, LoanStatus status) throws SQLException {
        try (Connection conn = DatabaseSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO LOAN (loan_id, reader_id, librarian_id, loan_date, due_date, return_date, status) "
                                + "VALUES (?,?,?,?,?,?,?) "
                                + "ON CONFLICT(loan_id) DO UPDATE SET "
                                + "reader_id=excluded.reader_id, "
                                + "librarian_id=excluded.librarian_id, "
                                + "loan_date=excluded.loan_date, "
                                + "due_date=excluded.due_date, "
                                + "return_date=excluded.return_date, "
                                + "status=excluded.status")) {
            stmt.setString(1, loanId);
            stmt.setString(2, readerId);
            stmt.setString(3, librarianId);
            stmt.setString(4, loanDate.toString());
            stmt.setString(5, dueDate.toString());
            stmt.setString(6, returnDate == null ? null : returnDate.toString());
            stmt.setString(7, status.name());
            stmt.executeUpdate();
        }
    }

    public static String dumpLoan(String loanId) throws SQLException {
        try (Connection conn = DatabaseSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT loan_id, reader_id, due_date, return_date, status FROM LOAN WHERE loan_id = ?")) {
            stmt.setString(1, loanId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return "(not present)";
                }
                return String.format("loan=%s | reader=%s | due=%s | return=%s | status=%s",
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5));
            }
        }
    }

    public static int countReader() throws SQLException {
        try (Connection conn = DatabaseSupport.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM READER");
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
