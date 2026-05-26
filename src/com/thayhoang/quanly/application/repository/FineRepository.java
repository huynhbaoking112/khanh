package com.thayhoang.quanly.application.repository;

import com.thayhoang.quanly.domain.model.Fine;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface FineRepository {
    Optional<Fine> findByLoanId(String loanId) throws SQLException;

    Optional<Fine> findByLoanId(Connection connection, String loanId) throws SQLException;

    Fine saveOrUpdate(Connection connection, Fine fine) throws SQLException;

    int count() throws SQLException;
}
