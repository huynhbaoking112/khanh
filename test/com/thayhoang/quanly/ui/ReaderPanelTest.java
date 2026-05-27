package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Reader;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderPanelTest {

    private static class FakeReaderService implements ReaderManagementService {
        final List<Reader> store = new ArrayList<>();

        @Override
        public List<Reader> listReaders() { return new ArrayList<>(store); }

        @Override
        public java.util.Optional<Reader> getReader(String readerId) { return store.stream().filter(r -> r.readerId().equals(readerId)).findFirst(); }

        @Override
        public int getCurrentBorrowCount(String readerId) { return 0; }

        @Override
        public Reader createReader(Reader reader) { store.add(reader); return reader; }

        @Override
        public Reader updateReader(Reader reader) {
            for (int i = 0; i < store.size(); i++) if (store.get(i).readerId().equals(reader.readerId())) { store.set(i, reader); return reader; }
            store.add(reader);
            return reader;
        }
    }

    @Test
    public void refreshAndCreateReader() throws Exception {
        FakeReaderService svc = new FakeReaderService();
        LibraryShellFrame.ReaderPanel panel = new LibraryShellFrame.ReaderPanel(svc, true);

        JSplitPane split = (JSplitPane) panel.getComponent(1);
        JScrollPane left = (JScrollPane) split.getLeftComponent();
        JTable table = (JTable) left.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        assertEquals(0, model.getRowCount());

        svc.createReader(new Reader("R100", "Name", "0901", "a@b.com", 3, ReaderStatus.ACTIVE));
        var m = LibraryShellFrame.ReaderPanel.class.getDeclaredMethod("refreshTable");
        m.setAccessible(true);
        m.invoke(panel);
        assertEquals(1, model.getRowCount());

        // test createReader via invoking method after setting fields
        var idFieldF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("idField");
        idFieldF.setAccessible(true);
        idFieldF.set(panel, new JTextField("R200"));
        var nameFieldF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("nameField");
        nameFieldF.setAccessible(true);
        nameFieldF.set(panel, new JTextField("New Reader"));
        var phoneFieldF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("phoneField");
        phoneFieldF.setAccessible(true);
        phoneFieldF.set(panel, new JTextField("0902"));
        var emailFieldF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("emailField");
        emailFieldF.setAccessible(true);
        emailFieldF.set(panel, new JTextField("e@x.com"));
        var maxBorrowFieldF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("maxBorrowField");
        maxBorrowFieldF.setAccessible(true);
        maxBorrowFieldF.set(panel, new JTextField("4"));
        var statusBoxF = (java.lang.reflect.Field) LibraryShellFrame.ReaderPanel.class.getDeclaredField("statusBox");
        statusBoxF.setAccessible(true);
        statusBoxF.set(panel, new JComboBox<>(ReaderStatus.values()));

        var createMethod = LibraryShellFrame.ReaderPanel.class.getDeclaredMethod("createReader");
        createMethod.setAccessible(true);
        createMethod.invoke(panel);

        assertTrue(svc.store.stream().anyMatch(r -> r.readerId().equals("R200")));
    }
}
