package com.thayhoang.quanly.infrastructure.repository.jdbc;

import com.thayhoang.quanly.application.repository.FineRepository;
import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.infrastructure.db.DatabaseSupport;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class JdbcFineRepository implements FineRepository {
    @Override
    public Optional<Fine> findByLoanId(String loanId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection()) {
            return findByLoanId(connection, loanId);
        }
    }

    @Override
    public Optional<Fine> findByLoanId(Connection connection, String loanId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT fine_id, loan_id, amount, reason, paid_status
                FROM FINE
                WHERE loan_id = ?
                """)) {
            statement.setString(1, loanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public Fine saveOrUpdate(Connection connection, Fine fine) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO FINE (fine_id, loan_id, amount, reason, paid_status)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(loan_id) DO UPDATE SET
                    amount = excluded.amount,
                    reason = excluded.reason,
                    paid_status = excluded.paid_status
                """)) {
            statement.setString(1, fine.fineId());
            statement.setString(2, fine.loanId());
            statement.setBigDecimal(3, fine.amount());
            statement.setString(4, fine.reason());
            statement.setString(5, fine.paidStatus().name());
            statement.executeUpdate();
            return fine;
        }
    }

    @Override
    public int count() throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM FINE");
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private Fine mapRow(ResultSet resultSet) throws SQLException {
        BigDecimal amount = resultSet.getBigDecimal("amount");
        return new Fine(
                resultSet.getString("fine_id"),
                resultSet.getString("loan_id"),
                amount == null ? BigDecimal.ZERO : amount,
                resultSet.getString("reason"),
                FinePaymentStatus.valueOf(resultSet.getString("paid_status")));
    }
}
