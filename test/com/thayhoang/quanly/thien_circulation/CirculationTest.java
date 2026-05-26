package com.thayhoang.quanly.thien_circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.service.CirculationService;
import com.thayhoang.quanly.application.service.dto.LoanReceipt;
import com.thayhoang.quanly.application.service.dto.ReturnReceipt;
import com.thayhoang.quanly.application.service.impl.CirculationServiceImpl;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.model.Loan;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CirculationTest {
    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("TC19 - create loan sets due date to loan date + 7 and decrements stock")
    void tc19_createLoanSetsDueDateAndDecrementsStock() throws SQLException {
        String readerId = "CR19";
        String bookId = "CB19";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Circulation Reader Active", "0901090019", "circulation.reader.active@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertBook(bookId, "Circulation Book Loan", "Pham Hung Thien", "NXB Test", 2024, 2, 2, BookStatus.AVAILABLE);

        CirculationService service = new CirculationServiceImpl(
                new JdbcReaderRepository(),
                new JdbcLibrarianRepository(),
                new JdbcBookRepository(),
                new JdbcLoanRepository(),
                new JdbcLoanDetailRepository(),
                new JdbcFineRepository());

        LoanReceipt receipt = service.createLoan(readerId, "LIB001", List.of(bookId));
        Book afterBook = new JdbcBookRepository().findById(bookId).orElseThrow();

        assertEquals(receipt.loan().loanDate().plusDays(CirculationRules.DEFAULT_LOAN_DAYS), receipt.loan().dueDate());
        assertEquals(1, receipt.details().size());
        assertEquals(1, afterBook.quantityAvailable());

        QcReport.tc("TC19", "Acceptance")
                .requirement("FR09 - Tao phieu muon moi, han 7 ngay")
                .dataset("TD15")
                .precondition("Doc gia ACTIVE va sach con kha dung")
                .input("readerId=CR19, bookId=CB19; action=Tao phieu muon")
                .expected("due_date = loan_date + 7 va quantity_available giam 1")
                .actual("loanId=" + receipt.loan().loanId() + ", due=" + receipt.loan().dueDate()
                        + ", bookAvailableAfter=" + afterBook.quantityAvailable())
                .dbBefore(TestDbHelper.dumpReader(readerId) + " || " + TestDbHelper.dumpBook(bookId))
                .dbAfter(TestDbHelper.dumpReader(readerId) + " || " + TestDbHelper.dumpBook(bookId))
                .pass();
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

        CirculationService service = new CirculationServiceImpl(
                new JdbcReaderRepository(),
                new JdbcLibrarianRepository(),
                new JdbcBookRepository(),
                new JdbcLoanRepository(),
                new JdbcLoanDetailRepository(),
                new JdbcFineRepository());

        LoanReceipt firstReceipt = service.createLoan(readerId, "LIB001", List.of(firstBookId));
        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
                () -> service.createLoan(readerId, "LIB001", List.of(secondBookId)));

        assertEquals("Doc gia da vuot gioi han muon sach", exception.getMessage());

        QcReport.tc("TC21", "Unit")
                .requirement("FR09 - Chan vuot ngưỡng maxBorrow")
                .dataset("TD14")
                .precondition("Reader maxBorrow=1, da co 1 loan active")
                .input("readerId=CR21, bookId=CB21B; action=Tao phieu muon")
                .expected("Exception: Doc gia da vuot gioi han muon sach")
                .actual("firstLoanId=" + firstReceipt.loan().loanId() + ", exception=" + exception.getMessage())
                .pass();
    }

    @Test
    @DisplayName("TC22 - returning a book completes the loan and restores stock")
    void tc22_returningBookCompletesLoanAndRestoresStock() throws SQLException {
        String readerId = "CR22";
        String bookId = "CB22";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Circulation Reader Return", "0901090022", "circulation.reader.return@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertBook(bookId, "Circulation Book Return", "Pham Hung Thien", "NXB Test", 2024, 1, 1, BookStatus.AVAILABLE);

        CirculationService service = new CirculationServiceImpl(
                new JdbcReaderRepository(),
                new JdbcLibrarianRepository(),
                new JdbcBookRepository(),
                new JdbcLoanRepository(),
                new JdbcLoanDetailRepository(),
                new JdbcFineRepository());

        LoanReceipt loanReceipt = service.createLoan(readerId, "LIB001", List.of(bookId));
        ReturnReceipt returnReceipt = service.returnBooks(loanReceipt.loan().loanId(), List.of(bookId), loanReceipt.loan().dueDate().minusDays(1));
        Book afterBook = new JdbcBookRepository().findById(bookId).orElseThrow();

        assertEquals(LoanStatus.COMPLETED, returnReceipt.loan().status());
        assertEquals(1, afterBook.quantityAvailable());
        assertTrue(returnReceipt.fine().isEmpty());

        QcReport.tc("TC22", "Integration")
                .requirement("FR10 - Tra sach dung han va cap nhat kho")
                .dataset("TD15")
                .precondition("Loan ACTIVE, sach dang cho muon")
                .input("loanId=" + loanReceipt.loan().loanId() + ", bookId=" + bookId + "; action=Xu ly tra sach")
                .expected("Loan -> COMPLETED, book quantity_available +1")
                .actual("status=" + returnReceipt.loan().status() + ", bookAvailableAfter=" + afterBook.quantityAvailable())
                .pass();
    }

    @Test
    @DisplayName("TC23 - overdue return creates a fine of 5000 per day")
    void tc23_overdueReturnCreatesFine() throws SQLException {
        String readerId = "CR23";
        String bookId = "CB23";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Circulation Reader Fine", "0901090023", "circulation.reader.fine@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertBook(bookId, "Circulation Book Fine", "Pham Hung Thien", "NXB Test", 2024, 1, 1, BookStatus.AVAILABLE);

        CirculationService service = new CirculationServiceImpl(
                new JdbcReaderRepository(),
                new JdbcLibrarianRepository(),
                new JdbcBookRepository(),
                new JdbcLoanRepository(),
                new JdbcLoanDetailRepository(),
                new JdbcFineRepository());

        LoanReceipt loanReceipt = service.createLoan(readerId, "LIB001", List.of(bookId));
        LocalDate overdueReturnDate = loanReceipt.loan().dueDate().plusDays(3);
        ReturnReceipt returnReceipt = service.returnBooks(loanReceipt.loan().loanId(), List.of(bookId), overdueReturnDate);
        Fine fine = returnReceipt.fine().orElseThrow();

        assertEquals(new BigDecimal("15000"), fine.amount());
        assertEquals(FinePaymentStatus.UNPAID, fine.paidStatus());
        assertEquals(LoanStatus.COMPLETED, returnReceipt.loan().status());

        QcReport.tc("TC23", "Acceptance")
                .requirement("FR11 - Tinh phat tre han 5000/ngay")
                .dataset("TD15")
                .precondition("Loan qua han 3 ngay")
                .input("loanId=" + loanReceipt.loan().loanId() + ", returnDate=" + overdueReturnDate + "; action=Xu ly tra sach")
                .expected("Tien phat = 3 * 5000 = 15000, status phat UNPAID")
                .actual("fineAmount=" + fine.amount() + ", fineStatus=" + fine.paidStatus())
                .pass();
    }

    @Test
    @DisplayName("TC24 - renew loan extends due date by the requested number of days")
    void tc24_renewLoanExtendsDueDate() throws SQLException {
        String readerId = "CR24";
        String bookId = "CB24";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Circulation Reader Renew", "0901090024", "circulation.reader.renew@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertBook(bookId, "Circulation Book Renew", "Pham Hung Thien", "NXB Test", 2024, 1, 1, BookStatus.AVAILABLE);

        CirculationService service = new CirculationServiceImpl(
                new JdbcReaderRepository(),
                new JdbcLibrarianRepository(),
                new JdbcBookRepository(),
                new JdbcLoanRepository(),
                new JdbcLoanDetailRepository(),
                new JdbcFineRepository());

        LoanReceipt loanReceipt = service.createLoan(readerId, "LIB001", List.of(bookId));
        Loan renewedLoan = service.renewLoan(loanReceipt.loan().loanId(), 7, loanReceipt.loan().loanDate());

        assertEquals(loanReceipt.loan().dueDate().plusDays(7), renewedLoan.dueDate());
        assertEquals(LoanStatus.ACTIVE, renewedLoan.status());

        QcReport.tc("TC24", "Unit")
                .requirement("FR12 - Gia han phieu muon hop le")
                .dataset("TD15")
                .precondition("Loan ACTIVE, con sach dang muon")
                .input("loanId=" + loanReceipt.loan().loanId() + ", extraDays=7; action=Gia han")
                .expected("due_date.plusDays(7) va status van ACTIVE")
                .actual("renewedDueDate=" + renewedLoan.dueDate() + ", status=" + renewedLoan.status())
                .pass();
    }
}