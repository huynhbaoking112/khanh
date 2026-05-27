package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Reader;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderPanelReadFormTest {

    private static class FakeReaderService implements ReaderManagementService {
        final List<Reader> store = new ArrayList<>();

        @Override
        public List<Reader> listReaders() { return new ArrayList<>(store); }

        @Override
        public java.util.Optional<Reader> getReader(String readerId) { return store.stream().filter(r -> r.readerId().equals(readerId)).findFirst(); }

        @Override
        public Reader createReader(Reader reader) { store.add(reader); return reader; }

        @Override
        public Reader updateReader(Reader reader) { return createReader(reader); }

        @Override
        public int getCurrentBorrowCount(String readerId) { return 0; }
    }

    @Test
    public void readReaderFromForm_parsesValuesCorrectly() throws Exception {
        FakeReaderService svc = new FakeReaderService();
        LibraryShellFrame.ReaderPanel panel = new LibraryShellFrame.ReaderPanel(svc, true);

        var idF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("idField");
        idF.setAccessible(true);
        idF.set(panel, new JTextField("RZ10"));

        var nameF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("nameField");
        nameF.setAccessible(true);
        nameF.set(panel, new JTextField("Reader Name"));

        var phoneF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("phoneField");
        phoneF.setAccessible(true);
        phoneF.set(panel, new JTextField("0912345678"));

        var emailF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("emailField");
        emailF.setAccessible(true);
        emailF.set(panel, new JTextField("a@b.com"));

        var maxF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("maxBorrowField");
        maxF.setAccessible(true);
        maxF.set(panel, new JTextField("4"));

        var statusF = LibraryShellFrame.ReaderPanel.class.getDeclaredField("statusBox");
        statusF.setAccessible(true);
        JComboBox<ReaderStatus> box = new JComboBox<>(ReaderStatus.values());
        box.setSelectedItem(ReaderStatus.ACTIVE);
        statusF.set(panel, box);

        var method = LibraryShellFrame.ReaderPanel.class.getDeclaredMethod("readReaderFromForm");
        method.setAccessible(true);
        Reader r = (Reader) method.invoke(panel);

        assertEquals("RZ10", r.readerId());
        assertEquals("Reader Name", r.fullName());
        assertEquals("0912345678", r.phone());
        assertEquals("a@b.com", r.email());
        assertEquals(4, r.maxBorrow());
        assertEquals(ReaderStatus.ACTIVE, r.status());
    }
}
