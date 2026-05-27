package com.thayhoang.quanly.application.service.impl;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.domain.model.Reader;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderManagementServiceImplTest {

    private Reader sampleReader() {
        return new Reader("R1", "Name", "090", "e@x", 1, com.thayhoang.quanly.domain.enums.ReaderStatus.ACTIVE);
    }

    @Test
    public void listReaders_sqlException_throws() {
        var readerRepo = new com.thayhoang.quanly.util.TestStubs.ReaderRepoStub() {
            @Override public java.util.List<Reader> findAll() throws SQLException { throw new SQLException("fail"); }
        };
        var service = new ReaderManagementServiceImpl(readerRepo, com.thayhoang.quanly.util.TestStubs.loanDetailRepoEmpty());
        assertThrows(ApplicationException.class, service::listReaders);
    }

    @Test
    public void createReader_validation_duplicate_and_success() throws SQLException {
        var emptyRepo = com.thayhoang.quanly.util.TestStubs.readerRepoEmpty();
        var service = new ReaderManagementServiceImpl(emptyRepo, com.thayhoang.quanly.util.TestStubs.loanDetailRepoEmpty());

        assertThrows(BusinessRuleViolationException.class, () -> service.createReader(null));
        assertThrows(BusinessRuleViolationException.class, () -> service.createReader(new Reader("R2", "N", "", "e@x", 1, com.thayhoang.quanly.domain.enums.ReaderStatus.ACTIVE)));

        var dupRepo = new com.thayhoang.quanly.util.TestStubs.ReaderRepoStub() {
            @Override public Optional<Reader> findById(String readerId) throws SQLException { return Optional.of(sampleReader()); }
        };
        var dupService = new ReaderManagementServiceImpl(dupRepo, com.thayhoang.quanly.util.TestStubs.loanDetailRepoEmpty());
        assertThrows(BusinessRuleViolationException.class, () -> dupService.createReader(sampleReader()));

        var okRepo = new com.thayhoang.quanly.util.TestStubs.ReaderRepoStub() {};
        var okService = new ReaderManagementServiceImpl(okRepo, com.thayhoang.quanly.util.TestStubs.loanDetailRepoEmpty());
        Reader created = okService.createReader(sampleReader());
        assertEquals("R1", created.readerId());
    }

    @Test
    public void updateReader_and_borrowCount_error() throws SQLException {
        var missingRepo = com.thayhoang.quanly.util.TestStubs.readerRepoEmpty();
        var service = new ReaderManagementServiceImpl(missingRepo, com.thayhoang.quanly.util.TestStubs.loanDetailRepoEmpty());
        assertThrows(BusinessRuleViolationException.class, () -> service.updateReader(sampleReader()));

        var countingRepo = new com.thayhoang.quanly.util.TestStubs.LoanDetailRepoStub() {
            @Override public int countActiveBorrowedBooksForReader(String readerId) throws SQLException { throw new SQLException("fail"); }
        };
        var borrowService = new ReaderManagementServiceImpl(com.thayhoang.quanly.util.TestStubs.readerRepoEmpty(), countingRepo);
        assertThrows(ApplicationException.class, () -> borrowService.getCurrentBorrowCount("R1"));
    }
}
