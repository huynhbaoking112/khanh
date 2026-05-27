package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.service.BookCatalogService;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookPanelBranchesTest {

    @Test
    public void refreshTable_callsSearchBooks_withAvailableOnlyTrue() throws Exception {
        final boolean[] searched = {false};
        BookCatalogService fake = new BookCatalogService() {
            @Override public List<Book> listBooks() { return List.of(); }
            @Override public List<Book> searchBooks(String keyword, boolean onlyAvailable) {
                searched[0] = onlyAvailable;
                return List.of(new Book("S1","T","A","P",2020,1,1, BookStatus.AVAILABLE));
            }
            @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
            @Override public Book createBook(Book b) { throw new UnsupportedOperationException(); }
            @Override public Book updateBook(Book b) { throw new UnsupportedOperationException(); }
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);

        Field searchField = LibraryShellFrame.BookPanel.class.getDeclaredField("searchField");
        Field availableCheck = LibraryShellFrame.BookPanel.class.getDeclaredField("availableOnlyCheck");
        searchField.setAccessible(true); availableCheck.setAccessible(true);

        JTextField sf = (JTextField) searchField.get(panel);
        JCheckBox cb = (JCheckBox) availableCheck.get(panel);

        SwingUtilities.invokeAndWait(() -> {
            sf.setText("abc");
            cb.setSelected(true);
        });

        Method refresh = LibraryShellFrame.BookPanel.class.getDeclaredMethod("refreshTable");
        refresh.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try { refresh.invoke(panel); } catch (Exception ex) { throw new RuntimeException(ex); }
        });

        assertTrue(searched[0]);
    }

    @Test
    public void readBookFromForm_throwsOnInvalidNumericFields() throws Exception {
        BookCatalogService fake = new BookCatalogService() {
            @Override public List<Book> listBooks() { return List.of(); }
            @Override public List<Book> searchBooks(String keyword, boolean onlyAvailable) { return List.of(); }
            @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
            @Override public Book createBook(Book b) { throw new UnsupportedOperationException(); }
            @Override public Book updateBook(Book b) { throw new UnsupportedOperationException(); }
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);

        Field yearField = LibraryShellFrame.BookPanel.class.getDeclaredField("yearField");
        Field totalField = LibraryShellFrame.BookPanel.class.getDeclaredField("totalField");
        Field availableField = LibraryShellFrame.BookPanel.class.getDeclaredField("availableField");
        yearField.setAccessible(true); totalField.setAccessible(true); availableField.setAccessible(true);

        JTextField y = (JTextField) yearField.get(panel);
        JTextField tot = (JTextField) totalField.get(panel);
        JTextField av = (JTextField) availableField.get(panel);

        SwingUtilities.invokeAndWait(() -> {
            y.setText("not-a-number");
            tot.setText("x");
            av.setText("");
        });

        Method read = LibraryShellFrame.BookPanel.class.getDeclaredMethod("readBookFromForm");
        read.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () -> {
            try { read.invoke(panel); } catch (java.lang.reflect.InvocationTargetException ite) { throw ite.getCause(); }
        });

        assertTrue(ex instanceof ApplicationException || ex instanceof NumberFormatException);
    }
}
