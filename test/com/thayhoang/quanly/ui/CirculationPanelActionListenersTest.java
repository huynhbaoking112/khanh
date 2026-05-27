package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.CirculationService;
import com.thayhoang.quanly.application.service.HistoryService;
import com.thayhoang.quanly.application.service.dto.AuthenticatedSession;
import com.thayhoang.quanly.application.service.dto.LoanReceipt;
import com.thayhoang.quanly.application.service.dto.ReturnReceipt;
import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Librarian;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.UserRole;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CirculationPanelActionListenersTest {

    @Test
    public void createLoanButton_triggersCreateLoanAndUpdatesReceipt() throws Exception {
        final String[] called = {null};
        CirculationService fakeCirculation = new CirculationService() {
            @Override
            public LoanReceipt createLoan(String readerId, String librarianId, List<String> bookIds) {
                called[0] = readerId + "|" + String.join(",", bookIds);
                Loan loan = new Loan("L1", readerId, librarianId, LocalDate.now(), LocalDate.now().plusDays(14), null, LoanStatus.ACTIVE);
                LoanDetail detail = new LoanDetail("LD1", "L1", "B1", 1, false, "");
                return new LoanReceipt(loan, List.of(detail));
            }

            @Override
            public ReturnReceipt returnBooks(String loanId, List<String> returnedBookIds, LocalDate returnDate) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Loan renewLoan(String loanId, int extraDays, LocalDate referenceDate) {
                throw new UnsupportedOperationException();
            }
        };

        HistoryService fakeHistory = new HistoryService() {
            @Override
            public List<LoanRecord> getReaderHistory(String readerId) { return List.of(); }

            @Override
            public Optional<LoanRecord> getLoanRecord(String loanId) { return Optional.empty(); }
        };

        AuthenticatedSession session = new AuthenticatedSession("LIB1", "Librarian One", "lib", UserRole.LIBRARIAN);
        LibraryShellFrame.CirculationPanel panel = new LibraryShellFrame.CirculationPanel(fakeCirculation, fakeHistory, session);

        // set fields
        setTextField(panel, "loanReaderField", "R1");
        setTextField(panel, "loanBooksField", "B1,B2");

        JButton createBtn = (JButton) findButtonByText(panel, "Tao phieu muon");
        assertNotNull(createBtn, "Create button not found");

        SwingUtilities.invokeAndWait(() -> createBtn.doClick());

        assertNotNull(called[0]);
        assertTrue(called[0].startsWith("R1|"));

        JTextArea receipt = (JTextArea) getField(panel, "receiptArea");
        assertNotNull(receipt.getText());
        assertTrue(receipt.getText().contains("L1"));
    }

    @Test
    public void returnButton_withEmptyBooks_usesHistoryServiceAndUpdatesReceipt() throws Exception {
        final String[] returnedBooks = {null};
        CirculationService fakeCirculation = new CirculationService() {
            @Override
            public LoanReceipt createLoan(String readerId, String librarianId, List<String> bookIds) { throw new UnsupportedOperationException(); }

            @Override
            public ReturnReceipt returnBooks(String loanId, List<String> returnedBookIds, LocalDate returnDate) {
                returnedBooks[0] = String.join(",", returnedBookIds);
                Loan loan = new Loan(loanId, "R2", "LIB2", LocalDate.now().minusDays(10), LocalDate.now().minusDays(3), LocalDate.now(), LoanStatus.COMPLETED);
                Fine fine = new Fine("F1", loanId, BigDecimal.valueOf(150000), "Late", FinePaymentStatus.UNPAID);
                LoanDetail detail = new LoanDetail("LD2", loanId, "B2", 1, true, "");
                return new ReturnReceipt(loan, List.of(detail), Optional.of(fine));
            }

            @Override
            public Loan renewLoan(String loanId, int extraDays, LocalDate referenceDate) { throw new UnsupportedOperationException(); }
        };

        HistoryService fakeHistory = new HistoryService() {
            @Override
            public List<LoanRecord> getReaderHistory(String readerId) { return List.of(); }

            @Override
            public Optional<LoanRecord> getLoanRecord(String loanId) {
                Loan loan = new Loan(loanId, "R2", "LIB2", LocalDate.now().minusDays(10), LocalDate.now().minusDays(3), null, LoanStatus.ACTIVE);
                LoanDetail d1 = new LoanDetail("LD2", loanId, "B2", 1, false, "");
                Reader reader = new Reader("R2", "Reader Two", "090", "a@b.com", 5, null);
                Librarian lib = new Librarian("LIB2", "Lib Two", "l2", "p", UserRole.LIBRARIAN);
                return Optional.of(new LoanRecord(loan, reader, lib, List.of(d1), Optional.empty()));
            }
        };

        AuthenticatedSession session = new AuthenticatedSession("LIB2", "Librarian Two", "lib2", UserRole.LIBRARIAN);
        LibraryShellFrame.CirculationPanel panel = new LibraryShellFrame.CirculationPanel(fakeCirculation, fakeHistory, session);

        setTextField(panel, "returnLoanIdField", "LN-1");
        setTextField(panel, "returnBooksField", ""); // empty -> should call history

        JButton returnBtn = (JButton) findButtonByText(panel, "Xu ly tra sach");
        assertNotNull(returnBtn);

        SwingUtilities.invokeAndWait(() -> returnBtn.doClick());

        assertNotNull(returnedBooks[0]);
        assertTrue(returnedBooks[0].contains("B2"));

        JTextArea receipt = (JTextArea) getField(panel, "receiptArea");
        assertTrue(receipt.getText().contains("Tien phat"));
    }

    @Test
    public void renewButton_triggersRenewAndShowsSuccess() throws Exception {
        CirculationService fakeCirculation = new CirculationService() {
            @Override
            public LoanReceipt createLoan(String readerId, String librarianId, List<String> bookIds) { throw new UnsupportedOperationException(); }

            @Override
            public ReturnReceipt returnBooks(String loanId, List<String> returnedBookIds, LocalDate returnDate) { throw new UnsupportedOperationException(); }

            @Override
            public Loan renewLoan(String loanId, int extraDays, LocalDate referenceDate) {
                return new Loan(loanId, "R3", "LIB3", LocalDate.now().minusDays(5), LocalDate.now().plusDays(7), null, LoanStatus.ACTIVE);
            }
        };

        HistoryService fakeHistory = new HistoryService() {
            @Override
            public List<LoanRecord> getReaderHistory(String readerId) { return List.of(); }

            @Override
            public Optional<LoanRecord> getLoanRecord(String loanId) { return Optional.empty(); }
        };

        AuthenticatedSession session = new AuthenticatedSession("LIB3", "Librarian Three", "lib3", UserRole.LIBRARIAN);
        LibraryShellFrame.CirculationPanel panel = new LibraryShellFrame.CirculationPanel(fakeCirculation, fakeHistory, session);

        setTextField(panel, "renewLoanIdField", "LN-RENEW");
        setTextField(panel, "renewDaysField", "7");

        JButton renewBtn = (JButton) findButtonByText(panel, "Gia han");
        assertNotNull(renewBtn);

        SwingUtilities.invokeAndWait(() -> renewBtn.doClick());

        JTextArea receipt = (JTextArea) getField(panel, "receiptArea");
        assertTrue(receipt.getText().contains("Gia han thanh cong"));
    }

    @Test
    public void returnButton_withProvidedBooks_andNoFine_showsTienPhatZero_andEmptyReturnDate() throws Exception {
        CirculationService fakeCirculation = new CirculationService() {
            @Override
            public LoanReceipt createLoan(String readerId, String librarianId, List<String> bookIds) { throw new UnsupportedOperationException(); }

            @Override
            public ReturnReceipt returnBooks(String loanId, List<String> returnedBookIds, LocalDate returnDate) {
                // simulate no fine and null return date in loan
                Loan loan = new Loan(loanId, "R4", "LIB4", LocalDate.now().minusDays(10), LocalDate.now().minusDays(3), null, LoanStatus.COMPLETED);
                LoanDetail detail = new LoanDetail("LD3", loanId, "B3", 1, true, "");
                return new ReturnReceipt(loan, List.of(detail), Optional.empty());
            }

            @Override
            public Loan renewLoan(String loanId, int extraDays, LocalDate referenceDate) { throw new UnsupportedOperationException(); }
        };

        HistoryService fakeHistory = new HistoryService() {
            @Override
            public List<LoanRecord> getReaderHistory(String readerId) { return List.of(); }

            @Override
            public Optional<LoanRecord> getLoanRecord(String loanId) { return Optional.empty(); }
        };

        AuthenticatedSession session = new AuthenticatedSession("LIB4", "Librarian Four", "lib4", UserRole.LIBRARIAN);
        LibraryShellFrame.CirculationPanel panel = new LibraryShellFrame.CirculationPanel(fakeCirculation, fakeHistory, session);

        setTextField(panel, "returnLoanIdField", "LN-2");
        setTextField(panel, "returnBooksField", "B3"); // provided, so should not call history

        JButton returnBtn = (JButton) findButtonByText(panel, "Xu ly tra sach");
        assertNotNull(returnBtn);

        SwingUtilities.invokeAndWait(() -> returnBtn.doClick());

        JTextArea receipt = (JTextArea) getField(panel, "receiptArea");
        assertTrue(receipt.getText().contains("Tien phat: 0"));
    }

    private static Component findButtonByText(Container c, String text) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton) {
                JButton b = (JButton) comp;
                if (text.equals(b.getText())) return b;
            }
            if (comp instanceof Container) {
                Component r = findButtonByText((Container) comp, text);
                if (r != null) return r;
            }
        }
        return null;
    }

    private static void setTextField(Object obj, String fieldName, String value) throws Exception {
        JTextField f = (JTextField) getField(obj, fieldName);
        f.setText(value);
    }

    private static Object getField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
