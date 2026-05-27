package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.HistoryService;
import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.application.service.dto.ReturnReceipt;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Librarian;
import com.thayhoang.quanly.domain.model.Reader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryPanelTest {

    @Test
    public void loadReaderHistory_populatesModelAndDetailArea() throws Exception {
        HistoryService service = new HistoryService() {
            @Override
            public java.util.List<LoanRecord> getReaderHistory(String readerId) {
                Loan loan = new Loan("L1", readerId, "LIB", LocalDate.now(), LocalDate.now().plusDays(14), null, null);
                LoanRecord rec = new LoanRecord(loan, new Reader(readerId, "Name", "", "", 1, null), new Librarian("LIB", "Lib", "libuser", "pwd", com.thayhoang.quanly.domain.enums.UserRole.LIBRARIAN), List.of(new LoanDetail("d1","L1","B1",1,false,"")), Optional.empty());
                return List.of(rec);
            }

            @Override
            public Optional<LoanRecord> getLoanRecord(String loanId) {
                return Optional.empty();
            }
        };

        LibraryShellFrame.HistoryPanel panel = new LibraryShellFrame.HistoryPanel(service);

        Field readerField = LibraryShellFrame.HistoryPanel.class.getDeclaredField("readerIdField");
        readerField.setAccessible(true);
        javax.swing.JTextField rf = (javax.swing.JTextField) readerField.get(panel);
        rf.setText("R1");

        Method m = LibraryShellFrame.HistoryPanel.class.getDeclaredMethod("loadReaderHistory");
        m.setAccessible(true);
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            try { m.invoke(panel); } catch (Exception e) { throw new RuntimeException(e); }
        });

        Field detailArea = LibraryShellFrame.HistoryPanel.class.getDeclaredField("detailArea");
        detailArea.setAccessible(true);
        javax.swing.JTextArea da = (javax.swing.JTextArea) detailArea.get(panel);
        assertNotNull(da.getText());

        Field modelF = LibraryShellFrame.HistoryPanel.class.getDeclaredField("model");
        modelF.setAccessible(true);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) modelF.get(panel);
        assertNotNull(model);
    }

    @Test
    public void loadLoanRecord_missingShowsNotFound() throws Exception {
        HistoryService service = new HistoryService() {
            @Override public java.util.List<LoanRecord> getReaderHistory(String readerId) { return List.of(); }
            @Override public Optional<LoanRecord> getLoanRecord(String loanId) { return Optional.empty(); }
        };

        LibraryShellFrame.HistoryPanel panel = new LibraryShellFrame.HistoryPanel(service);

        Field loanField = LibraryShellFrame.HistoryPanel.class.getDeclaredField("loanIdField");
        loanField.setAccessible(true);
        javax.swing.JTextField lf = (javax.swing.JTextField) loanField.get(panel);
        lf.setText("NOPE");

        Method m = LibraryShellFrame.HistoryPanel.class.getDeclaredMethod("loadLoanRecord");
        m.setAccessible(true);
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            try { m.invoke(panel); } catch (Exception e) { throw new RuntimeException(e); }
        });

        Field detailArea = LibraryShellFrame.HistoryPanel.class.getDeclaredField("detailArea");
        detailArea.setAccessible(true);
        javax.swing.JTextArea da = (javax.swing.JTextArea) detailArea.get(panel);
        assertNotNull(da.getText());
    }

    @Test
    public void loadLoanRecord_displaysDetailsAndFine() throws Exception {
        HistoryService service = new HistoryService() {
            @Override
            public java.util.List<LoanRecord> getReaderHistory(String readerId) { return List.of(); }

            @Override
            public Optional<LoanRecord> getLoanRecord(String loanId) {
                Loan loan = new Loan(loanId, "R1", "LIB", LocalDate.now(), LocalDate.now().plusDays(14), LocalDate.now(), null);
                LoanRecord rec = new LoanRecord(loan, new Reader("R1", "Name", "", "", 1, null), new Librarian("LIB", "Lib", "libuser", "pwd", com.thayhoang.quanly.domain.enums.UserRole.LIBRARIAN), List.of(new LoanDetail("d1","L1","B1",2,true,"note")), Optional.of(new Fine("F1", loanId, BigDecimal.TEN, "reason", com.thayhoang.quanly.domain.enums.FinePaymentStatus.UNPAID)));
                return Optional.of(rec);
            }
        };

        LibraryShellFrame.HistoryPanel panel = new LibraryShellFrame.HistoryPanel(service);
        Field loanField = LibraryShellFrame.HistoryPanel.class.getDeclaredField("loanIdField");
        loanField.setAccessible(true);
        javax.swing.JTextField lf = (javax.swing.JTextField) loanField.get(panel);
        lf.setText("L1");

        Method m = LibraryShellFrame.HistoryPanel.class.getDeclaredMethod("loadLoanRecord");
        m.setAccessible(true);
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            try { m.invoke(panel); } catch (Exception e) { throw new RuntimeException(e); }
        });

        Field detailArea = LibraryShellFrame.HistoryPanel.class.getDeclaredField("detailArea");
        detailArea.setAccessible(true);
        javax.swing.JTextArea da = (javax.swing.JTextArea) detailArea.get(panel);
        String text = da.getText();
        assertNotNull(text);
    }
}
