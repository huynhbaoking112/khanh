package com.thayhoang.quanly.application.repository;

import com.thayhoang.quanly.domain.model.LoanDetail;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface LoanDetailRepository {
    List<LoanDetail> findByLoanId(String loanId) throws SQLException;

    List<LoanDetail> findByLoanId(Connection connection, String loanId) throws SQLException;

    Optional<LoanDetail> findByLoanAndBookId(Connection connection, String loanId, String bookId) throws SQLException;

    LoanDetail save(Connection connection, LoanDetail loanDetail) throws SQLException;

    LoanDetail update(Connection connection, LoanDetail loanDetail) throws SQLException;

    int countActiveBorrowedBooksForReader(String readerId) throws SQLException;

    int countActiveBorrowedBooksForReader(Connection connection, String readerId) throws SQLException;

    int count() throws SQLException;
}
