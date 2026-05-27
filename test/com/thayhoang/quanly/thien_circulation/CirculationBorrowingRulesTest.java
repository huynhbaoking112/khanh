package com.thayhoang.quanly.thien_circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.service.CirculationService;
import com.thayhoang.quanly.application.service.dto.LoanReceipt;
import com.thayhoang.quanly.application.service.impl.CirculationServiceImpl;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.rules.CirculationRules;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcBookRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcFineRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLibrarianRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLoanDetailRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLoanRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcReaderRepository;
import com.thayhoang.quanly.testsupport.QcReport;
import com.thayhoang.quanly.testsupport.TestDbHelper;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CirculationBorrowingRulesTest {
    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("TC20 - locked reader is rejected from borrowing")
    void tc20_lockedReaderIsRejectedFromBorrowing() {
        Reader lockedReader = new Reader("CR20", "Circulation Reader Locked", "0901090020", "circulation.reader.locked@example.com", 5, ReaderStatus.INACTIVE);

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
                () -> CirculationRules.validateReaderForBorrowing(lockedReader, 0, 1));

        assertEquals("Doc gia khong o trang thai hoat dong", exception.getMessage());

        QcReport.tc("TC20", "Unit")
                .requirement("FR09 - Chan doc gia khong ACTIVE")
                .dataset("TD13")
                .precondition("Reader status=LOCKED/INACTIVE")
                .input("readerId=CR20, bookId=CB20; action=Tao phieu muon")
                .expected("BusinessRuleViolationException: Doc gia khong o trang thai hoat dong")
                .actual("exception=" + exception.getMessage())
                .pass();
    }

    @Test
    @DisplayName("TC21 - borrowing beyond maxBorrow is rejected")
    void tc21_borrowingBeyondLimitIsRejected() throws SQLException {
        String readerId = "CR21";
        String firstBookId = "CB21A";
        String secondBookId = "CB21B";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Circulation Reader Limit", "0901090021", "circulation.reader.limit@example.com", 1, ReaderStatus.ACTIVE);
        TestDbHelper.upsertBook(firstBookId, "Circulation Book First", "Pham Hung Thien", "NXB Test", 2024, 1, 1, BookStatus.AVAILABLE);
        TestDbHelper.upsertBook(secondBookId, "Circulation Book Second", "Pham Hung Thien", "NXB Test", 2024, 1, 1, BookStatus.AVAILABLE);

        CirculationService service = newService();

        LoanReceipt firstReceipt = service.createLoan(readerId, "LIB001", List.of(firstBookId));
        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
                () -> service.createLoan(readerId, "LIB001", List.of(secondBookId)));

        assertEquals("Doc gia da vuot gioi han muon sach", exception.getMessage());

        QcReport.tc("TC21", "Unit")
                .requirement("FR09 - Chan vuot ngÆ°á»¡ng maxBorrow")
                .dataset("TD14")
                .precondition("Reader maxBorrow=1, da co 1 loan active")
                .input("readerId=CR21, bookId=CB21B; action=Tao phieu muon")
                .expected("Exception: Doc gia da vuot gioi han muon sach")
                .actual("firstLoanId=" + firstReceipt.loan().loanId() + ", exception=" + exception.getMessage())
                .pass();
    }

    private static CirculationService newService() {
        return new CirculationServiceImpl(
                new JdbcReaderRepository(),
                new JdbcLibrarianRepository(),
                new JdbcBookRepository(),
                new JdbcLoanRepository(),
                new JdbcLoanDetailRepository(),
                new JdbcFineRepository());
    }
}
