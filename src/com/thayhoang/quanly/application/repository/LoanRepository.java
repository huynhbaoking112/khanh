package com.thayhoang.quanly.application.repository;

import com.thayhoang.quanly.domain.model.Loan;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    Optional<Loan> findById(String loanId) throws SQLException;

    Optional<Loan> findById(Connection connection, String loanId) throws SQLException;

    List<Loan> findByReaderId(String readerId) throws SQLException;

    Loan save(Connection connection, Loan loan) throws SQLException;

    Loan update(Connection connection, Loan loan) throws SQLException;

    int count() throws SQLException;
}
