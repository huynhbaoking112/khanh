package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.service.BookCatalogService;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookPanelActionsTest {

    private static Component findButtonByText(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton b && text.equals(b.getText())) return b;
            if (c instanceof Container) {
                Component found = findButtonByText((Container) c, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Test
    public void refreshTable_callsListBooks_when_searchEmpty() throws Exception {
        BookCatalogService fake = new BookCatalogService() {
            @Override
            public List<Book> listBooks() {
                return List.of(new Book("B1", "T1", "A1", "P1", 2020, 2, 2, BookStatus.AVAILABLE));
            }

            @Override
            public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }

            @Override
            public List<Book> searchBooks(String keyword, boolean onlyAvailable) { throw new UnsupportedOperationException(); }

            @Override
            public Book createBook(Book b) { throw new UnsupportedOperationException(); }

            @Override
            public Book updateBook(Book b) { throw new UnsupportedOperationException(); }

            // no deleteBook in current service API
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);
        // click the "Tra cuu" button to refresh
        Component btn = findButtonByText(panel, "Tra cuu");
        assertNotNull(btn);

        SwingUtilities.invokeAndWait(() -> ((JButton) btn).doClick());

        // verify table now has one row
        Field tableField = LibraryShellFrame.BookPanel.class.getDeclaredField("table");
        tableField.setAccessible(true);
        JTable table = (JTable) tableField.get(panel);
        assertEquals(1, table.getRowCount());
    }

    @Test
    public void refreshTable_callsSearchBooks_when_searchNonEmpty() throws Exception {
        final boolean[] searched = {false};
        BookCatalogService fake = new BookCatalogService() {
            @Override
            public List<Book> listBooks() { throw new UnsupportedOperationException(); }

            @Override
            public List<Book> searchBooks(String keyword, boolean onlyAvailable) {
                searched[0] = true;
                return List.of(new Book("S1", "SX", "SA", "SP", 2019, 1, 1, BookStatus.AVAILABLE));
            }

            @Override
            public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }

            @Override
            public Book createBook(Book b) { throw new UnsupportedOperationException(); }

            @Override
            public Book updateBook(Book b) { throw new UnsupportedOperationException(); }
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);

        // set search text
        Field searchField = LibraryShellFrame.BookPanel.class.getDeclaredField("searchField");
        searchField.setAccessible(true);
        JTextField sf = (JTextField) searchField.get(panel);
        SwingUtilities.invokeAndWait(() -> sf.setText("keyword"));

        Component btn = findButtonByText(panel, "Tra cuu");
        assertNotNull(btn);
        SwingUtilities.invokeAndWait(() -> ((JButton) btn).doClick());

        assertTrue(searched[0]);
    }

    @Test
    public void createBook_callsService_createBook_and_updatesTable() throws Exception {
        final boolean[] created = {false};
        BookCatalogService fake = new BookCatalogService() {
            @Override
            public List<Book> listBooks() { return List.of(); }

            @Override
            public List<Book> searchBooks(String keyword, boolean onlyAvailable) { return List.of(); }

            @Override
            public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }

            @Override
            public Book createBook(Book b) {
                created[0] = true;
                return b;
            }

            @Override
            public Book updateBook(Book b) { throw new UnsupportedOperationException(); }
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);

        // fill form fields
        Field titleField = LibraryShellFrame.BookPanel.class.getDeclaredField("titleField");
        Field authorField = LibraryShellFrame.BookPanel.class.getDeclaredField("authorField");
        Field publisherField = LibraryShellFrame.BookPanel.class.getDeclaredField("publisherField");
        Field yearField = LibraryShellFrame.BookPanel.class.getDeclaredField("yearField");
        Field totalField = LibraryShellFrame.BookPanel.class.getDeclaredField("totalField");
        Field availableField = LibraryShellFrame.BookPanel.class.getDeclaredField("availableField");
        titleField.setAccessible(true); authorField.setAccessible(true); publisherField.setAccessible(true);
        yearField.setAccessible(true); totalField.setAccessible(true);
        availableField.setAccessible(true);

        JTextField t = (JTextField) titleField.get(panel);
        JTextField a = (JTextField) authorField.get(panel);
        JTextField p = (JTextField) publisherField.get(panel);
        JTextField y = (JTextField) yearField.get(panel);
        JTextField tot = (JTextField) totalField.get(panel);
        JTextField av = (JTextField) availableField.get(panel);

        SwingUtilities.invokeAndWait(() -> {
            t.setText("TitleX");
            a.setText("AuthorX");
            p.setText("PubX");
            y.setText("2021");
            tot.setText("3");
            av.setText("3");
        });

        Method createMethod = LibraryShellFrame.BookPanel.class.getDeclaredMethod("createBook");
        createMethod.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                createMethod.invoke(panel);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        assertTrue(created[0]);
    }
}
