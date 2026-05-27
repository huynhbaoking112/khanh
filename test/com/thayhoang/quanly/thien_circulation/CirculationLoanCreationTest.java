package com.thayhoang.quanly.thien_circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thayhoang.quanly.application.service.CirculationService;
import com.thayhoang.quanly.application.service.dto.LoanReceipt;
import com.thayhoang.quanly.application.service.impl.CirculationServiceImpl;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Book;
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

class CirculationLoanCreationTest {
    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("TC19 - create loan sets due date to loan date + 14 and decrements stock")
    void tc19_createLoanSetsDueDateAndDecrementsStock() throws SQLException {
        String readerId = "CR19";
        String bookId = "CB19";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Circulation Reader Active", "0901090019", "circulation.reader.active@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertBook(bookId, "Circulation Book Loan", "Pham Hung Thien", "NXB Test", 2024, 2, 2, BookStatus.AVAILABLE);

        CirculationService service = newService();

        LoanReceipt receipt = service.createLoan(readerId, "LIB001", List.of(bookId));
        Book afterBook = new JdbcBookRepository().findById(bookId).orElseThrow();

        assertEquals(receipt.loan().loanDate().plusDays(CirculationRules.DEFAULT_LOAN_DAYS), receipt.loan().dueDate());
        assertEquals(1, receipt.details().size());
        assertEquals(1, afterBook.quantityAvailable());

        QcReport.tc("TC19", "Acceptance")
                .requirement("FR09 - Tao phieu muon moi, han 14 ngay")
                .dataset("TD15")
                .precondition("Doc gia ACTIVE va sach con kha dung")
                .input("readerId=CR19, bookId=CB19; action=Tao phieu muon")
                .expected("due_date = loan_date + 14 va quantity_available giam 1")
                .actual("loanId=" + receipt.loan().loanId() + ", due=" + receipt.loan().dueDate()
                        + ", bookAvailableAfter=" + afterBook.quantityAvailable())
                .dbBefore(TestDbHelper.dumpReader(readerId) + " || " + TestDbHelper.dumpBook(bookId))
                .dbAfter(TestDbHelper.dumpReader(readerId) + " || " + TestDbHelper.dumpBook(bookId))
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
