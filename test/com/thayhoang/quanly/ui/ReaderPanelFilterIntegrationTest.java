package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderPanelFilterIntegrationTest {

    @Test
    public void filter_addsAndClearsRowFilter() throws Exception {
        ReaderManagementService svc = new ReaderManagementService() {
            @Override public List<Reader> listReaders() {
                return List.of(new Reader("R1","Khoa Match","090","a@b",1, ReaderStatus.ACTIVE),
                        new Reader("R2","NoMatch","091","c@d",1, ReaderStatus.ACTIVE));
            }
            @Override public java.util.Optional<Reader> getReader(String readerId) { return java.util.Optional.empty(); }
            @Override public Reader createReader(Reader reader) { return reader; }
            @Override public Reader updateReader(Reader reader) { return reader; }
            @Override public int getCurrentBorrowCount(String readerId) { return 0; }
        };

        final LibraryShellFrame.ReaderPanel[] panel = new LibraryShellFrame.ReaderPanel[1];
        SwingUtilities.invokeAndWait(() -> panel[0] = new LibraryShellFrame.ReaderPanel(svc, true));

        java.lang.reflect.Field filterField = panel[0].getClass().getDeclaredField("filterField");
        filterField.setAccessible(true);
        java.lang.reflect.Field tableField = panel[0].getClass().getDeclaredField("table");
        tableField.setAccessible(true);

        // set filter text to trigger regexFilter
        SwingUtilities.invokeAndWait(() -> {
            try { ((javax.swing.JTextField) filterField.get(panel[0])).setText("khoa"); } catch (Exception e) { throw new RuntimeException(e); }
        });
        SwingUtilities.invokeAndWait(() -> {
            try { var sorter = (TableRowSorter<?>) ((javax.swing.JTable) tableField.get(panel[0])).getRowSorter(); assertNotNull(sorter.getRowFilter()); } catch (Exception e) { throw new RuntimeException(e); }
        });

        // clear filter
        SwingUtilities.invokeAndWait(() -> {
            try { ((javax.swing.JTextField) filterField.get(panel[0])).setText(""); } catch (Exception e) { throw new RuntimeException(e); }
        });
        SwingUtilities.invokeAndWait(() -> {
            try { var sorter = (TableRowSorter<?>) ((javax.swing.JTable) tableField.get(panel[0])).getRowSorter(); assertNull(sorter.getRowFilter()); } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
