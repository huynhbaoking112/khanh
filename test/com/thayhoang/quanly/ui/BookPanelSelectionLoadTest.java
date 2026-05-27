package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookPanelSelectionLoadTest {

    private static class DummyService implements BookCatalogService {
        @Override public List<Book> listBooks() {
            return List.of(new Book("BK1","T","A","P",2022,5,5, BookStatus.AVAILABLE));
        }
        @Override public List<Book> searchBooks(String keyword, boolean onlyAvailable) { return listBooks(); }
        @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
        @Override public Book createBook(Book book) { throw new UnsupportedOperationException(); }
        @Override public Book updateBook(Book book) { throw new UnsupportedOperationException(); }
    }

    @Test
    public void selectingTableRow_loadsFormFields() throws Exception {
        DummyService svc = new DummyService();
        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(svc, true);

        Field tableField = LibraryShellFrame.BookPanel.class.getDeclaredField("table");
        tableField.setAccessible(true);
        JTable table = (JTable) tableField.get(panel);

        assertTrue(table.getRowCount() >= 1);

        SwingUtilities.invokeAndWait(() -> table.setRowSelectionInterval(0,0));
        SwingUtilities.invokeAndWait(() -> {});

        Field idF = LibraryShellFrame.BookPanel.class.getDeclaredField("idField");
        idF.setAccessible(true);
        JTextField idField = (JTextField) idF.get(panel);
        assertEquals("BK1", idField.getText());
    }
}
