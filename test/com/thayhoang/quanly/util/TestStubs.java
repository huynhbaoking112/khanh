package com.thayhoang.quanly.util;

import com.thayhoang.quanly.application.repository.*;
import com.thayhoang.quanly.domain.model.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class TestStubs {
    private TestStubs() {}

    public abstract static class ReaderRepoStub implements com.thayhoang.quanly.application.repository.ReaderRepository {
        @Override public Optional<Reader> findById(String readerId) throws SQLException { return Optional.empty(); }
        @Override public Optional<Reader> findById(Connection connection, String readerId) throws SQLException { return Optional.empty(); }
        @Override public List<Reader> findAll() throws SQLException { return List.of(); }
        @Override public Reader save(Reader reader) throws SQLException { return reader; }
        @Override public Reader update(Reader reader) throws SQLException { return reader; }
        @Override public int count() throws SQLException { return 0; }
        @Override public List<Reader> findOverdueReaders(LocalDate today) throws SQLException { return List.of(); }
    }

    public abstract static class LibrarianRepoStub implements com.thayhoang.quanly.application.repository.LibrarianRepository {
        @Override public Optional<Librarian> findById(String librarianId) throws SQLException { return Optional.empty(); }
        @Override public Optional<Librarian> findById(Connection connection, String librarianId) throws SQLException { return Optional.empty(); }
        @Override public Optional<Librarian> findByUsername(String username) throws SQLException { return Optional.empty(); }
        @Override public List<Librarian> findAll() throws SQLException { return List.of(); }
        @Override public Librarian save(Librarian librarian) throws SQLException { return librarian; }
        @Override public int count() throws SQLException { return 0; }
    }

    public abstract static class LoanRepoStub implements com.thayhoang.quanly.application.repository.LoanRepository {
        @Override public Optional<Loan> findById(String loanId) throws SQLException { return Optional.empty(); }
        @Override public Optional<Loan> findById(Connection connection, String loanId) throws SQLException { return Optional.empty(); }
        @Override public List<Loan> findByReaderId(String readerId) throws SQLException { return List.of(); }
        @Override public Loan save(Connection connection, Loan loan) throws SQLException { return loan; }
        @Override public Loan update(Connection connection, Loan loan) throws SQLException { return loan; }
        @Override public int count() throws SQLException { return 0; }
    }

    public abstract static class LoanDetailRepoStub implements com.thayhoang.quanly.application.repository.LoanDetailRepository {
        @Override public List<LoanDetail> findByLoanId(String loanId) throws SQLException { return List.of(); }
        @Override public List<LoanDetail> findByLoanId(Connection connection, String loanId) throws SQLException { return List.of(); }
        @Override public Optional<LoanDetail> findByLoanAndBookId(Connection connection, String loanId, String bookId) throws SQLException { return Optional.empty(); }
        @Override public LoanDetail save(Connection connection, LoanDetail loanDetail) throws SQLException { return loanDetail; }
        @Override public LoanDetail update(Connection connection, LoanDetail loanDetail) throws SQLException { return loanDetail; }
        @Override public int countActiveBorrowedBooksForReader(String readerId) throws SQLException { return 0; }
        @Override public int countActiveBorrowedBooksForReader(Connection connection, String readerId) throws SQLException { return 0; }
        @Override public int count() throws SQLException { return 0; }
    }

    public abstract static class FineRepoStub implements com.thayhoang.quanly.application.repository.FineRepository {
        @Override public Optional<Fine> findByLoanId(String loanId) throws SQLException { return Optional.empty(); }
        @Override public Optional<Fine> findByLoanId(Connection connection, String loanId) throws SQLException { return Optional.empty(); }
        @Override public Fine saveOrUpdate(Connection connection, Fine fine) throws SQLException { return fine; }
        @Override public int count() throws SQLException { return 0; }
    }

    public static com.thayhoang.quanly.application.repository.ReaderRepository readerRepoEmpty() {
        return new ReaderRepoStub() {};
    }

    public static com.thayhoang.quanly.application.repository.LibrarianRepository librarianRepoEmpty() {
        return new LibrarianRepoStub() {};
    }

    public static com.thayhoang.quanly.application.repository.LoanRepository loanRepoEmpty() {
        return new LoanRepoStub() {};
    }

    public static com.thayhoang.quanly.application.repository.LoanDetailRepository loanDetailRepoEmpty() {
        return new LoanDetailRepoStub() {};
    }

    public static com.thayhoang.quanly.application.repository.FineRepository fineRepoEmpty() {
        return new FineRepoStub() {};
    }
}
