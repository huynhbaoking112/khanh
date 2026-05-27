package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Reader;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderPanelSelectionLoadTest {

    private static class DummyService implements ReaderManagementService {
        @Override public List<Reader> listReaders() { return List.of(new Reader("R1","Name","090","e@e",3, ReaderStatus.ACTIVE)); }
        @Override public Reader createReader(Reader r) { throw new UnsupportedOperationException(); }
        @Override public Reader updateReader(Reader r) { throw new UnsupportedOperationException(); }
        @Override public java.util.Optional<Reader> getReader(String readerId) { return java.util.Optional.empty(); }
        @Override public int getCurrentBorrowCount(String readerId) { return 0; }
    }

    @Test
    public void selectingTableRow_loadsReaderFormFields() throws Exception {
        DummyService svc = new DummyService();
        LibraryShellFrame.ReaderPanel panel = new LibraryShellFrame.ReaderPanel(svc, true);

        Field tableField = LibraryShellFrame.ReaderPanel.class.getDeclaredField("table");
        tableField.setAccessible(true);
        JTable table = (JTable) tableField.get(panel);

        assertTrue(table.getRowCount() >= 1);
        SwingUtilities.invokeAndWait(() -> table.setRowSelectionInterval(0,0));
        SwingUtilities.invokeAndWait(() -> {});

        Field idF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("idField");
        idF.setAccessible(true);
        JTextField idField = (JTextField) idF.get(panel);
        assertEquals("R1", idField.getText());
    }
}
