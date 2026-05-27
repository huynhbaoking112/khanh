package com.thayhoang.quanly.thien_circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class CirculationReturnTest {
    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("TC22 - returning a book completes the loan and restores stock")
    void tc22_returningBookCompletesLoanAndRestoresStock() throws SQLException {
        String readerId = "CR22";
        String bookId = "CB22";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Circulation Reader Return", "0901090022", "circulation.reader.return@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertBook(bookId, "Circulation Book Return", "Pham Hung Thien", "NXB Test", 2024, 1, 1, BookStatus.AVAILABLE);

        CirculationService service = newService();

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
    @DisplayName("TC23 - overdue return creates a fine of 50000 per day")
    void tc23_overdueReturnCreatesFine() throws SQLException {
        String readerId = "CR23";
        String bookId = "CB23";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Circulation Reader Fine", "0901090023", "circulation.reader.fine@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertBook(bookId, "Circulation Book Fine", "Pham Hung Thien", "NXB Test", 2024, 1, 1, BookStatus.AVAILABLE);

        CirculationService service = newService();

        LoanReceipt loanReceipt = service.createLoan(readerId, "LIB001", List.of(bookId));
        LocalDate overdueReturnDate = loanReceipt.loan().dueDate().plusDays(3);
        ReturnReceipt returnReceipt = service.returnBooks(loanReceipt.loan().loanId(), List.of(bookId), overdueReturnDate);
        Fine fine = returnReceipt.fine().orElseThrow();

        assertEquals(new BigDecimal("150000"), fine.amount());
        assertEquals(FinePaymentStatus.UNPAID, fine.paidStatus());
        assertEquals(LoanStatus.COMPLETED, returnReceipt.loan().status());

        QcReport.tc("TC23", "Acceptance")
                .requirement("FR11 - Tinh phat tre han 50000/ngay")
                .dataset("TD15")
                .precondition("Loan qua han 3 ngay")
                .input("loanId=" + loanReceipt.loan().loanId() + ", returnDate=" + overdueReturnDate + "; action=Xu ly tra sach")
                .expected("Tien phat = 3 * 50000 = 150000, status phat UNPAID")
                .actual("fineAmount=" + fine.amount() + ", fineStatus=" + fine.paidStatus())
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
