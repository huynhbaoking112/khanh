package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.domain.enums.BookStatus;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookPanelSearchListCombosTest {

    @Test
    public void blankSearch_and_availableOnlyFalse_callsListBooks() throws Exception {
        final boolean[] listed = {false};
        BookCatalogService fake = new BookCatalogService() {
            @Override public List<Book> listBooks() { listed[0] = true; return List.of(); }
            @Override public List<Book> searchBooks(String keyword, boolean onlyAvailable) { return List.of(); }
            @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
            @Override public Book createBook(Book b) { throw new UnsupportedOperationException(); }
            @Override public Book updateBook(Book b) { throw new UnsupportedOperationException(); }
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);

        Method refresh = LibraryShellFrame.BookPanel.class.getDeclaredMethod("refreshTable");
        refresh.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try { refresh.invoke(panel); } catch (Exception ex) { throw new RuntimeException(ex); }
        });

        assertTrue(listed[0]);
    }

    @Test
    public void blankSearch_and_availableOnlyTrue_callsSearchBooks() throws Exception {
        final boolean[] searched = {false};
        BookCatalogService fake = new BookCatalogService() {
            @Override public List<Book> listBooks() { return List.of(); }
            @Override public List<Book> searchBooks(String keyword, boolean onlyAvailable) { searched[0] = onlyAvailable; return List.of(); }
            @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
            @Override public Book createBook(Book b) { throw new UnsupportedOperationException(); }
            @Override public Book updateBook(Book b) { throw new UnsupportedOperationException(); }
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);

        Field availableCheck = LibraryShellFrame.BookPanel.class.getDeclaredField("availableOnlyCheck");
        availableCheck.setAccessible(true);
        JCheckBox cb = (JCheckBox) availableCheck.get(panel);

        SwingUtilities.invokeAndWait(() -> cb.setSelected(true));

        Method refresh = LibraryShellFrame.BookPanel.class.getDeclaredMethod("refreshTable");
        refresh.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try { refresh.invoke(panel); } catch (Exception ex) { throw new RuntimeException(ex); }
        });

        assertTrue(searched[0]);
    }

    @Test
    public void nonEmptySearch_callsSearchBooks_withTrimmedText() throws Exception {
        final String[] got = {null};
        BookCatalogService fake = new BookCatalogService() {
            @Override public List<Book> listBooks() { return List.of(); }
            @Override public List<Book> searchBooks(String keyword, boolean onlyAvailable) { got[0] = keyword; return List.of(); }
            @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
            @Override public Book createBook(Book b) { throw new UnsupportedOperationException(); }
            @Override public Book updateBook(Book b) { throw new UnsupportedOperationException(); }
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);

        Field searchField = LibraryShellFrame.BookPanel.class.getDeclaredField("searchField");
        searchField.setAccessible(true);
        JTextField sf = (JTextField) searchField.get(panel);

        SwingUtilities.invokeAndWait(() -> sf.setText("  hello  "));

        Method refresh = LibraryShellFrame.BookPanel.class.getDeclaredMethod("refreshTable");
        refresh.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try { refresh.invoke(panel); } catch (Exception ex) { throw new RuntimeException(ex); }
        });

        assertEquals("hello", got[0]);
    }
}
