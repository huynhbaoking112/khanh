package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.service.CirculationService;
import com.thayhoang.quanly.application.service.HistoryService;
import com.thayhoang.quanly.application.service.dto.AuthenticatedSession;
import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.application.service.dto.LoanReceipt;
import com.thayhoang.quanly.application.service.dto.ReturnReceipt;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.model.Librarian;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CirculationPanelTest {

    @Test
    public void loadUnreturnedBookIds_returnsOnlyNotReturned() throws Exception {
        HistoryService historyService = new HistoryService() {
            @Override
            public java.util.List<com.thayhoang.quanly.application.service.dto.LoanRecord> getReaderHistory(String readerId) { return List.of(); }

            @Override
            public Optional<LoanRecord> getLoanRecord(String loanId) {
            return Optional.of(new LoanRecord(
                new Loan("L1", "R1", "LIB", LocalDate.now(), LocalDate.now().plusDays(7), null, null),
                new Reader("R1", "Reader", "", "", 1, null),
                new Librarian("LIB", "Lib", "libuser", "pwd", com.thayhoang.quanly.domain.enums.UserRole.LIBRARIAN),
                List.of(
                    new LoanDetail("d1", "L1", "B1", 1, false, ""),
                    new LoanDetail("d2", "L1", "B2", 1, true, "")
                ),
                Optional.empty()
            ));
            }
        };

        CirculationService circ = new CirculationService() {
            @Override public LoanReceipt createLoan(String readerId, String librarianId, List<String> bookIds) { return new LoanReceipt(null, List.of()); }
            @Override public ReturnReceipt returnBooks(String loanId, List<String> returnedBookIds, LocalDate returnDate) { return new ReturnReceipt(null, List.of(), Optional.empty()); }
            @Override public Loan renewLoan(String loanId, int extraDays, LocalDate referenceDate) { return null; }
        };

        AuthenticatedSession session = new AuthenticatedSession("LIB", "Lib", "libuser", UserRole.LIBRARIAN);
        LibraryShellFrame.CirculationPanel panel = new LibraryShellFrame.CirculationPanel(circ, historyService, session);

        Method m = LibraryShellFrame.CirculationPanel.class.getDeclaredMethod("loadUnreturnedBookIds", String.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<String> ids = (java.util.List<String>) m.invoke(panel, "L1");
        assertEquals(1, ids.size());
        assertEquals("B1", ids.get(0));
    }

    @Test
    public void loadUnreturnedBookIds_missingLoan_throws() throws Exception {
        HistoryService historyService = new HistoryService() {
            @Override public java.util.List<com.thayhoang.quanly.application.service.dto.LoanRecord> getReaderHistory(String readerId) { return List.of(); }
            @Override public Optional<LoanRecord> getLoanRecord(String loanId) { return Optional.empty(); }
        };
        CirculationService circ = new CirculationService() {
            @Override public LoanReceipt createLoan(String readerId, String librarianId, List<String> bookIds) { return new LoanReceipt(null, List.of()); }
            @Override public ReturnReceipt returnBooks(String loanId, List<String> returnedBookIds, LocalDate returnDate) { return new ReturnReceipt(null, List.of(), Optional.empty()); }
            @Override public Loan renewLoan(String loanId, int extraDays, LocalDate referenceDate) { return null; }
        };
        AuthenticatedSession session = new AuthenticatedSession("LIB", "Lib", "libuser", UserRole.LIBRARIAN);
        LibraryShellFrame.CirculationPanel panel = new LibraryShellFrame.CirculationPanel(circ, historyService, session);

        Method m = LibraryShellFrame.CirculationPanel.class.getDeclaredMethod("loadUnreturnedBookIds", String.class);
        m.setAccessible(true);
        Exception ex = assertThrows(Exception.class, () -> m.invoke(panel, "NOPE"));
        assertTrue(ex.getCause() instanceof ApplicationException);
    }

    @Test
    public void formatLoanAndReturnReceipt_producesText() throws Exception {
        CirculationService circ = new CirculationService() {
            @Override public LoanReceipt createLoan(String readerId, String librarianId, List<String> bookIds) {
                Loan loan = new Loan("L1", readerId, librarianId, LocalDate.now(), LocalDate.now().plusDays(7), null, null);
                return new LoanReceipt(loan, List.of(new LoanDetail("d1","L1","B1",1,false,"")));
            }
            @Override public ReturnReceipt returnBooks(String loanId, List<String> returnedBookIds, LocalDate returnDate) {
                Loan loan = new Loan(loanId, "R1", "LIB", LocalDate.now(), LocalDate.now().plusDays(7), returnDate, com.thayhoang.quanly.domain.enums.LoanStatus.COMPLETED);
                return new ReturnReceipt(loan, List.of(), Optional.empty());
            }
            @Override public Loan renewLoan(String loanId, int extraDays, LocalDate referenceDate) { return null; }
        };
        HistoryService historyService = new HistoryService() {
            @Override public java.util.List<com.thayhoang.quanly.application.service.dto.LoanRecord> getReaderHistory(String readerId) { return List.of(); }
            @Override public Optional<LoanRecord> getLoanRecord(String loanId) { return Optional.empty(); }
        };
        AuthenticatedSession session = new AuthenticatedSession("LIB", "Lib", "libuser", UserRole.LIBRARIAN);
        LibraryShellFrame.CirculationPanel panel = new LibraryShellFrame.CirculationPanel(circ, historyService, session);

        Method loanFmt = LibraryShellFrame.CirculationPanel.class.getDeclaredMethod("formatLoanReceipt", LoanReceipt.class);
        loanFmt.setAccessible(true);
        String loanText = (String) loanFmt.invoke(panel, circ.createLoan("R1", "LIB", List.of("B1")));
        assertNotNull(loanText);
        assertTrue(loanText.contains("L1") || loanText.contains("R1"));

        Method retFmt = LibraryShellFrame.CirculationPanel.class.getDeclaredMethod("formatReturnReceipt", ReturnReceipt.class);
        retFmt.setAccessible(true);
        String retText = (String) retFmt.invoke(panel, circ.returnBooks("L1", List.of("B1"), LocalDate.now()));
        assertNotNull(retText);
        assertTrue(retText.contains("Tien phat") || retText.contains("L1"));
    }
}
