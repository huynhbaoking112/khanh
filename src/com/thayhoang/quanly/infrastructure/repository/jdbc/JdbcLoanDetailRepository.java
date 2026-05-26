package com.thayhoang.quanly.infrastructure.repository.jdbc;

import com.thayhoang.quanly.application.repository.LoanDetailRepository;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.infrastructure.db.DatabaseSupport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcLoanDetailRepository implements LoanDetailRepository {
    @Override
    public List<LoanDetail> findByLoanId(String loanId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection()) {
            return findByLoanId(connection, loanId);
        }
    }

    @Override
    public List<LoanDetail> findByLoanId(Connection connection, String loanId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT loan_detail_id, loan_id, book_id, quantity, returned, note
                FROM LOAN_DETAIL
                WHERE loan_id = ?
                ORDER BY loan_detail_id
                """)) {
            statement.setString(1, loanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<LoanDetail> details = new ArrayList<>();
                while (resultSet.next()) {
                    details.add(mapRow(resultSet));
                }
                return List.copyOf(details);
            }
        }
    }

    @Override
    public Optional<LoanDetail> findByLoanAndBookId(Connection connection, String loanId, String bookId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT loan_detail_id, loan_id, book_id, quantity, returned, note
                FROM LOAN_DETAIL
                WHERE loan_id = ? AND book_id = ?
                """)) {
            statement.setString(1, loanId);
            statement.setString(2, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public LoanDetail save(Connection connection, LoanDetail loanDetail) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO LOAN_DETAIL (loan_detail_id, loan_id, book_id, quantity, returned, note)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            statement.setString(1, loanDetail.loanDetailId());
            statement.setString(2, loanDetail.loanId());
            statement.setString(3, loanDetail.bookId());
            statement.setInt(4, loanDetail.quantity());
            statement.setInt(5, loanDetail.returned() ? 1 : 0);
            statement.setString(6, loanDetail.note());
            statement.executeUpdate();
            return loanDetail;
        }
    }

    @Override
    public LoanDetail update(Connection connection, LoanDetail loanDetail) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE LOAN_DETAIL
                SET quantity = ?, returned = ?, note = ?
                WHERE loan_detail_id = ?
                """)) {
            statement.setInt(1, loanDetail.quantity());
            statement.setInt(2, loanDetail.returned() ? 1 : 0);
            statement.setString(3, loanDetail.note());
            statement.setString(4, loanDetail.loanDetailId());
            statement.executeUpdate();
            return loanDetail;
        }
    }

    @Override
    public int countActiveBorrowedBooksForReader(String readerId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection()) {
            return countActiveBorrowedBooksForReader(connection, readerId);
        }
    }

    @Override
    public int countActiveBorrowedBooksForReader(Connection connection, String readerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COALESCE(SUM(ld.quantity), 0)
                FROM LOAN_DETAIL ld
                JOIN LOAN l ON l.loan_id = ld.loan_id
                WHERE l.reader_id = ?
                  AND ld.returned = 0
                  AND l.status <> 'COMPLETED'
                """)) {
            statement.setString(1, readerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    @Override
    public int count() throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM LOAN_DETAIL");
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private LoanDetail mapRow(ResultSet resultSet) throws SQLException {
        return new LoanDetail(
                resultSet.getString("loan_detail_id"),
                resultSet.getString("loan_id"),
                resultSet.getString("book_id"),
                resultSet.getInt("quantity"),
                resultSet.getInt("returned") == 1,
                resultSet.getString("note"));
    }
}
