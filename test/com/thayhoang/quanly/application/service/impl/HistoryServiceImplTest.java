package com.thayhoang.quanly.application.service.impl;

import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryServiceImplTest {

    @Test
    public void getReaderHistory_readerNotFound_returnsEmpty() {
        var service = new HistoryServiceImpl(
                com.thayhoang.quanly.util.TestStubs.readerRepoEmpty(),
                com.thayhoang.quanly.util.TestStubs.librarianRepoEmpty(),
                com.thayhoang.quanly.util.TestStubs.loanRepoEmpty(),
                com.thayhoang.quanly.util.TestStubs.loanDetailRepoEmpty(),
                com.thayhoang.quanly.util.TestStubs.fineRepoEmpty());
        assertTrue(service.getReaderHistory("R1").isEmpty());
    }

    @Test
    public void getReaderHistory_skipsMissingLibrarian_and_returnsRecord() throws SQLException {
        Reader reader = new Reader("R1", "Reader", "090", "r@x", 1, ReaderStatus.ACTIVE);
        Loan loan = new Loan("L1", "R1", "LIB", LocalDate.now(), LocalDate.now().plusDays(14), null, LoanStatus.ACTIVE);

        var readerRepo = new com.thayhoang.quanly.util.TestStubs.ReaderRepoStub() {
            @Override public Optional<Reader> findById(String readerId) throws SQLException { return Optional.of(reader); }
        };
        var loanRepo = new com.thayhoang.quanly.util.TestStubs.LoanRepoStub() {
            @Override public List<Loan> findByReaderId(String readerId) throws SQLException { return List.of(loan); }
            @Override public Optional<Loan> findById(String loanId) throws SQLException { return Optional.of(loan); }
        };
        var librarianMissing = com.thayhoang.quanly.util.TestStubs.librarianRepoEmpty();
        var serviceMissing = new HistoryServiceImpl(readerRepo, librarianMissing, loanRepo, com.thayhoang.quanly.util.TestStubs.loanDetailRepoEmpty(), com.thayhoang.quanly.util.TestStubs.fineRepoEmpty());
        assertTrue(serviceMissing.getReaderHistory("R1").isEmpty());

        var librarianRepo = new com.thayhoang.quanly.util.TestStubs.LibrarianRepoStub() {
            @Override public Optional<Librarian> findById(String librarianId) throws SQLException { return Optional.of(new Librarian("LIB", "L", "u", "p", com.thayhoang.quanly.domain.enums.UserRole.LIBRARIAN)); }
        };
        var loanDetailRepo = new com.thayhoang.quanly.util.TestStubs.LoanDetailRepoStub() {
            @Override public List<LoanDetail> findByLoanId(String loanId) throws SQLException { return List.of(new LoanDetail("LD", "L1", "B1", 1, false, null)); }
        };
        var service = new HistoryServiceImpl(readerRepo, librarianRepo, loanRepo, loanDetailRepo, com.thayhoang.quanly.util.TestStubs.fineRepoEmpty());
        List<LoanRecord> records = service.getReaderHistory("R1");
        assertEquals(1, records.size());
        assertEquals("L1", records.get(0).loan().loanId());
    }

    @Test
    public void getLoanRecord_missingAndPresent() throws SQLException {
        Loan loan = new Loan("L1", "R1", "LIB", LocalDate.now(), LocalDate.now().plusDays(14), null, LoanStatus.ACTIVE);
        var loanRepo = new com.thayhoang.quanly.util.TestStubs.LoanRepoStub() {
            @Override public Optional<Loan> findById(String loanId) throws SQLException { return Optional.of(loan); }
        };
        var readerMissing = com.thayhoang.quanly.util.TestStubs.readerRepoEmpty();
        var librarianMissing = com.thayhoang.quanly.util.TestStubs.librarianRepoEmpty();
        var serviceMissing = new HistoryServiceImpl(readerMissing, librarianMissing, loanRepo, com.thayhoang.quanly.util.TestStubs.loanDetailRepoEmpty(), com.thayhoang.quanly.util.TestStubs.fineRepoEmpty());
        assertTrue(serviceMissing.getLoanRecord("L1").isEmpty());

        var readerRepo = new com.thayhoang.quanly.util.TestStubs.ReaderRepoStub() {
            @Override public Optional<Reader> findById(String readerId) throws SQLException { return Optional.of(new Reader("R1", "Reader", "090", "r@x", 1, ReaderStatus.ACTIVE)); }
        };
        var librarianRepo = new com.thayhoang.quanly.util.TestStubs.LibrarianRepoStub() {
            @Override public Optional<Librarian> findById(String librarianId) throws SQLException { return Optional.of(new Librarian("LIB", "L", "u", "p", com.thayhoang.quanly.domain.enums.UserRole.LIBRARIAN)); }
        };
        var loanDetailRepo = new com.thayhoang.quanly.util.TestStubs.LoanDetailRepoStub() {
            @Override public List<LoanDetail> findByLoanId(String loanId) throws SQLException { return List.of(new LoanDetail("LD", "L1", "B1", 1, false, null)); }
        };
        var fineRepo = new com.thayhoang.quanly.util.TestStubs.FineRepoStub() {
            @Override public Optional<Fine> findByLoanId(String loanId) throws SQLException {
                return Optional.of(new Fine("F1", "L1", BigDecimal.ZERO, "x", FinePaymentStatus.UNPAID));
            }
        };
        var service = new HistoryServiceImpl(readerRepo, librarianRepo, loanRepo, loanDetailRepo, fineRepo);
        Optional<LoanRecord> maybe = service.getLoanRecord("L1");
        assertTrue(maybe.isPresent());
        assertEquals("L1", maybe.get().loan().loanId());
    }
}
