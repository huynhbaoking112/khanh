package com.thayhoang.quanly.thien_circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thayhoang.quanly.application.service.CirculationService;
import com.thayhoang.quanly.application.service.dto.LoanReceipt;
import com.thayhoang.quanly.application.service.impl.CirculationServiceImpl;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Loan;
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

class CirculationRenewalTest {
    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("TC24 - renew loan extends due date by the requested number of days")
    void tc24_renewLoanExtendsDueDate() throws SQLException {
        String readerId = "CR24";
        String bookId = "CB24";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Circulation Reader Renew", "0901090024", "circulation.reader.renew@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertBook(bookId, "Circulation Book Renew", "Pham Hung Thien", "NXB Test", 2024, 1, 1, BookStatus.AVAILABLE);

        CirculationService service = newService();

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
