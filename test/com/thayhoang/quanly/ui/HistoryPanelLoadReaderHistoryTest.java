package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.HistoryService;
import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.model.Librarian;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryPanelLoadReaderHistoryTest {

    @Test
    public void loadReaderHistory_populatesTable() throws Exception {
        HistoryService svc = new HistoryService() {
            @Override public java.util.List<LoanRecord> getReaderHistory(String readerId) {
                Loan loan = new Loan("L1","R1","LIB", LocalDate.now(), LocalDate.now().plusDays(5), null, LoanStatus.COMPLETED);
                Reader r = new Reader("R1","N","090","e@x",1, ReaderStatus.ACTIVE);
                Librarian lib = new Librarian("LIB","L","u","p", com.thayhoang.quanly.domain.enums.UserRole.LIBRARIAN);
                LoanDetail d = new LoanDetail("LD","L1","B1",1,true,"");
                Fine fine = new Fine("F1","L1", BigDecimal.valueOf(500), "Late", FinePaymentStatus.UNPAID);
                LoanRecord rec = new LoanRecord(loan, r, lib, List.of(d), java.util.Optional.of(fine));
                return List.of(rec);
            }

            @Override public java.util.Optional<LoanRecord> getLoanRecord(String loanRecordId) { return java.util.Optional.empty(); }
        };

        final LibraryShellFrame.HistoryPanel[] panel = new LibraryShellFrame.HistoryPanel[1];
        SwingUtilities.invokeAndWait(() -> panel[0] = new LibraryShellFrame.HistoryPanel(svc));

        java.lang.reflect.Field readerIdField = panel[0].getClass().getDeclaredField("readerIdField");
        readerIdField.setAccessible(true);
        java.lang.reflect.Field tableField = panel[0].getClass().getDeclaredField("table");
        tableField.setAccessible(true);

        SwingUtilities.invokeAndWait(() -> {
            try { ((javax.swing.JTextField) readerIdField.get(panel[0])).setText("R1"); } catch (Exception e) { throw new RuntimeException(e); }
        });

        // call private loadReaderHistory
        java.lang.reflect.Method m = panel[0].getClass().getDeclaredMethod("loadReaderHistory");
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try { m.invoke(panel[0]); } catch (Exception e) { throw new RuntimeException(e); }
        });

        SwingUtilities.invokeAndWait(() -> {
            try {
                DefaultTableModel model = (DefaultTableModel) ((javax.swing.JTable) tableField.get(panel[0])).getModel();
                assertEquals(1, model.getRowCount());
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
