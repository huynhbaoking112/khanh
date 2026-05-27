package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Reader;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderPanelFilterTest {

    @Test
    public void filterField_updatesRowFilter_onInsertAndClear() throws Exception {
        ReaderManagementService fake = new ReaderManagementService() {
            @Override public List<Reader> listReaders() {
                return List.of(new Reader("R1","Nguyen Khoa", "0901", "a@a", 3, ReaderStatus.ACTIVE));
            }
            @Override public Reader createReader(Reader r) { throw new UnsupportedOperationException(); }
            @Override public Reader updateReader(Reader r) { throw new UnsupportedOperationException(); }
            @Override public java.util.Optional<Reader> getReader(String readerId) { return java.util.Optional.empty(); }
            @Override public int getCurrentBorrowCount(String readerId) { return 0; }
        };

        LibraryShellFrame.ReaderPanel panel = new LibraryShellFrame.ReaderPanel(fake, true);

        // ensure table has rows
        Field tableField = LibraryShellFrame.ReaderPanel.class.getDeclaredField("table");
        tableField.setAccessible(true);
        JTable table = (JTable) tableField.get(panel);
        // model should be filled by constructor->refreshTable
        assertTrue(table.getRowCount() >= 1);

        Field filterField = LibraryShellFrame.ReaderPanel.class.getDeclaredField("filterField");
        filterField.setAccessible(true);
        JTextField ff = (JTextField) filterField.get(panel);

        SwingUtilities.invokeAndWait(() -> ff.setText("khoa"));
        // give DocumentListener a moment via invokeAndWait
        SwingUtilities.invokeAndWait(() -> {});

        // verify sorter has a RowFilter applied
        assertNotNull(table.getRowSorter());
        @SuppressWarnings("unchecked")
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = (javax.swing.table.TableRowSorter<DefaultTableModel>) table.getRowSorter();
        assertNotNull(sorter.getRowFilter());

        // clear filter
        SwingUtilities.invokeAndWait(() -> ff.setText(""));
        SwingUtilities.invokeAndWait(() -> {});

        assertNull(sorter.getRowFilter());
    }
}
