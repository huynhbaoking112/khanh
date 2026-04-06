package com.thayhoang.quanly.infrastructure.repository.jdbc;

import com.thayhoang.quanly.application.repository.LoanRepository;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.infrastructure.db.DatabaseSupport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcLoanRepository implements LoanRepository {
    @Override
    public Optional<Loan> findById(String loanId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection()) {
            return findById(connection, loanId);
        }
    }

    @Override
    public Optional<Loan> findById(Connection connection, String loanId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT loan_id, reader_id, librarian_id, loan_date, due_date, return_date, status
                FROM LOAN
                WHERE loan_id = ?
                """)) {
            statement.setString(1, loanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Loan> findByReaderId(String readerId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT loan_id, reader_id, librarian_id, loan_date, due_date, return_date, status
                        FROM LOAN
                        WHERE reader_id = ?
                        ORDER BY loan_date DESC, loan_id DESC
                        """)) {
            statement.setString(1, readerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Loan> loans = new ArrayList<>();
                while (resultSet.next()) {
                    loans.add(mapRow(resultSet));
                }
                return List.copyOf(loans);
            }
        }
    }

    @Override
    public Loan save(Connection connection, Loan loan) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO LOAN (loan_id, reader_id, librarian_id, loan_date, due_date, return_date, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """)) {
            bind(statement, loan, false);
            statement.executeUpdate();
            return loan;
        }
    }

    @Override
    public Loan update(Connection connection, Loan loan) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE LOAN
                SET reader_id = ?, librarian_id = ?, loan_date = ?, due_date = ?, return_date = ?, status = ?
                WHERE loan_id = ?
                """)) {
            bind(statement, loan, true);
            statement.executeUpdate();
            return loan;
        }
    }

    @Override
    public int count() throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM LOAN");
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private void bind(PreparedStatement statement, Loan loan, boolean forUpdate) throws SQLException {
        int index = 1;
        if (!forUpdate) {
            statement.setString(index++, loan.loanId());
        }
        statement.setString(index++, loan.readerId());
        statement.setString(index++, loan.librarianId());
        statement.setString(index++, loan.loanDate().toString());
        statement.setString(index++, loan.dueDate().toString());
        statement.setString(index++, loan.returnDate() == null ? null : loan.returnDate().toString());
        statement.setString(index++, loan.status().name());
        if (forUpdate) {
            statement.setString(index, loan.loanId());
        }
    }

    private Loan mapRow(ResultSet resultSet) throws SQLException {
        String returnDate = resultSet.getString("return_date");
        return new Loan(
                resultSet.getString("loan_id"),
                resultSet.getString("reader_id"),
                resultSet.getString("librarian_id"),
                LocalDate.parse(resultSet.getString("loan_date")),
                LocalDate.parse(resultSet.getString("due_date")),
                returnDate == null ? null : LocalDate.parse(returnDate),
                LoanStatus.valueOf(resultSet.getString("status")));
    }
}
