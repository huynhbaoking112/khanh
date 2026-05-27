package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Reader;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderPanelDocumentListenerTest {

    @Test
    public void changedUpdate_isHandled_byDocumentListener() throws Exception {
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

        Field filterField = LibraryShellFrame.ReaderPanel.class.getDeclaredField("filterField");
        filterField.setAccessible(true);
        JTextField ff = (JTextField) filterField.get(panel);

        // set text to apply a filter
        SwingUtilities.invokeAndWait(() -> ff.setText("khoa"));
        SwingUtilities.invokeAndWait(() -> {});

        Field tableField = LibraryShellFrame.ReaderPanel.class.getDeclaredField("table");
        tableField.setAccessible(true);
        JTable table = (JTable) tableField.get(panel);
        @SuppressWarnings("unchecked")
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = (javax.swing.table.TableRowSorter<DefaultTableModel>) table.getRowSorter();
        assertNotNull(sorter.getRowFilter());

        // find the installed DocumentListener and call changedUpdate directly
        DocumentListener[] listeners;
        javax.swing.text.Document doc = ff.getDocument();
        if (doc instanceof javax.swing.text.AbstractDocument) {
            var m = doc.getClass().getMethod("getDocumentListeners");
            listeners = (DocumentListener[]) m.invoke(doc);
        } else {
            listeners = new DocumentListener[0];
        }
        assertTrue(listeners.length > 0);
        DocumentListener listener = listeners[0];

        DocumentEvent stub = new DocumentEvent() {
            @Override public int getOffset() { return 0; }
            @Override public int getLength() { return 0; }
            @Override public javax.swing.text.Document getDocument() { return ff.getDocument(); }
            @Override public EventType getType() { return EventType.CHANGE; }
            @Override public javax.swing.event.DocumentEvent.ElementChange getChange(javax.swing.text.Element elem) { return null; }
        };

        SwingUtilities.invokeAndWait(() -> listener.changedUpdate(stub));

        // filter should still be applied
        assertNotNull(sorter.getRowFilter());

        // clearing should remove the filter
        SwingUtilities.invokeAndWait(() -> ff.setText(""));
        SwingUtilities.invokeAndWait(() -> {});
        assertNull(sorter.getRowFilter());
    }
}
