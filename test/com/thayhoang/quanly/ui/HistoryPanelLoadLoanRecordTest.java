package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.HistoryService;
import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.model.Librarian;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Reader;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryPanelLoadLoanRecordTest {

    @Test
    public void loadLoanRecord_emptyAndPresent_updatesDetailArea() throws Exception {
        HistoryService svc = new HistoryService() {
            @Override
            public List<LoanRecord> getReaderHistory(String readerId) { return List.of(); }

            @Override
            public Optional<LoanRecord> getLoanRecord(String loanId) {
                if ("EMPTY".equals(loanId)) return Optional.empty();
                // else return a populated record
                Loan loan = new Loan("L1", "R1", "LIB1", LocalDate.now(), LocalDate.now().plusDays(14), null, LoanStatus.ACTIVE);
                Reader reader = new Reader("R1", "Reader One", "0901", "r1@example.com", 3, com.thayhoang.quanly.domain.enums.ReaderStatus.ACTIVE);
                Librarian lib = new Librarian("LIB1", "Lib One", "lib", "pw", com.thayhoang.quanly.domain.enums.UserRole.LIBRARIAN);
                LoanDetail detail = new LoanDetail("LD1", "L1", "B1", 1, false, "");
                Fine fine = new Fine("F1", "L1", BigDecimal.valueOf(150000), "Late", FinePaymentStatus.UNPAID);
                LoanRecord record = new LoanRecord(loan, reader, lib, List.of(detail), Optional.of(fine));
                return Optional.of(record);
            }
        };

        final LibraryShellFrame.HistoryPanel[] panel = new LibraryShellFrame.HistoryPanel[1];
        SwingUtilities.invokeAndWait(() -> panel[0] = new LibraryShellFrame.HistoryPanel(svc));

        java.lang.reflect.Field loanIdField = panel[0].getClass().getDeclaredField("loanIdField");
        loanIdField.setAccessible(true);
        java.lang.reflect.Field detailAreaField = panel[0].getClass().getDeclaredField("detailArea");
        detailAreaField.setAccessible(true);
        java.lang.reflect.Method m = panel[0].getClass().getDeclaredMethod("loadLoanRecord");
        m.setAccessible(true);

        // empty case
        SwingUtilities.invokeAndWait(() -> {
            try { ((javax.swing.JTextField) loanIdField.get(panel[0])).setText("EMPTY"); } catch (Exception e) { throw new RuntimeException(e); }
        });
        SwingUtilities.invokeAndWait(() -> {
            try { m.invoke(panel[0]); } catch (Exception e) { throw new RuntimeException(e); }
        });
        SwingUtilities.invokeAndWait(() -> {
            try { assertTrue(((javax.swing.JTextArea) detailAreaField.get(panel[0])).getText().contains("Khong tim thay phieu muon.")); } catch (Exception e) { throw new RuntimeException(e); }
        });

        // present case
        SwingUtilities.invokeAndWait(() -> {
            try { ((javax.swing.JTextField) loanIdField.get(panel[0])).setText("L1"); } catch (Exception e) { throw new RuntimeException(e); }
        });
        SwingUtilities.invokeAndWait(() -> {
            try { m.invoke(panel[0]); } catch (Exception e) { throw new RuntimeException(e); }
        });

        SwingUtilities.invokeAndWait(() -> {
            try {
                String txt = ((javax.swing.JTextArea) detailAreaField.get(panel[0])).getText();
                assertTrue(txt.contains("Phieu muon: L1"));
                assertTrue(txt.contains("- B1"));
                assertTrue(txt.contains("Phat:") || txt.contains("Khong co"));
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
