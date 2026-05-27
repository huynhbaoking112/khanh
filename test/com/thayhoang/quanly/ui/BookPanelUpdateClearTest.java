package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.application.service.BookCatalogService;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookPanelUpdateClearTest {

    @Test
    public void updateBook_callsService_updateBook() throws Exception {
        final boolean[] updated = {false};
        BookCatalogService fake = new BookCatalogService() {
            @Override
            public List<Book> listBooks() { return List.of(); }

            @Override
            public List<Book> searchBooks(String keyword, boolean onlyAvailable) { return List.of(); }

            @Override
            public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }

            @Override
            public Book createBook(Book b) { throw new UnsupportedOperationException(); }

            @Override
            public Book updateBook(Book b) {
                updated[0] = true;
                return b;
            }
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);

        // populate form
        Field idField = LibraryShellFrame.BookPanel.class.getDeclaredField("idField");
        Field titleField = LibraryShellFrame.BookPanel.class.getDeclaredField("titleField");
        Field authorField = LibraryShellFrame.BookPanel.class.getDeclaredField("authorField");
        Field publisherField = LibraryShellFrame.BookPanel.class.getDeclaredField("publisherField");
        Field yearField = LibraryShellFrame.BookPanel.class.getDeclaredField("yearField");
        Field totalField = LibraryShellFrame.BookPanel.class.getDeclaredField("totalField");
        Field availableField = LibraryShellFrame.BookPanel.class.getDeclaredField("availableField");

        idField.setAccessible(true); titleField.setAccessible(true); authorField.setAccessible(true);
        publisherField.setAccessible(true); yearField.setAccessible(true); totalField.setAccessible(true);
        availableField.setAccessible(true);

        JTextField id = (JTextField) idField.get(panel);
        JTextField t = (JTextField) titleField.get(panel);
        JTextField a = (JTextField) authorField.get(panel);
        JTextField p = (JTextField) publisherField.get(panel);
        JTextField y = (JTextField) yearField.get(panel);
        JTextField tot = (JTextField) totalField.get(panel);
        JTextField av = (JTextField) availableField.get(panel);

        SwingUtilities.invokeAndWait(() -> {
            id.setText("BKX");
            t.setText("TitleU");
            a.setText("AuthorU");
            p.setText("PubU");
            y.setText("2020");
            tot.setText("5");
            av.setText("5");
        });

        Method updateMethod = LibraryShellFrame.BookPanel.class.getDeclaredMethod("updateBook");
        updateMethod.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try { updateMethod.invoke(panel); } catch (Exception ex) { throw new RuntimeException(ex); }
        });

        assertTrue(updated[0]);
    }

    @Test
    public void clearForm_clearsAllFields_and_resetsStatus() throws Exception {
        BookCatalogService fake = new BookCatalogService() {
            @Override public List<Book> listBooks() { return List.of(); }
            @Override public List<Book> searchBooks(String keyword, boolean onlyAvailable) { return List.of(); }
            @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
            @Override public Book createBook(Book b) { throw new UnsupportedOperationException(); }
            @Override public Book updateBook(Book b) { throw new UnsupportedOperationException(); }
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);

        Field titleField = LibraryShellFrame.BookPanel.class.getDeclaredField("titleField");
        Field availableField = LibraryShellFrame.BookPanel.class.getDeclaredField("availableField");
        Field statusBox = LibraryShellFrame.BookPanel.class.getDeclaredField("statusBox");
        titleField.setAccessible(true); availableField.setAccessible(true); statusBox.setAccessible(true);

        JTextField t = (JTextField) titleField.get(panel);
        JTextField av = (JTextField) availableField.get(panel);
        @SuppressWarnings("unchecked")
        JComboBox<BookStatus> sb = (JComboBox<BookStatus>) statusBox.get(panel);

        SwingUtilities.invokeAndWait(() -> {
            t.setText("SomeTitle");
            av.setText("2");
            sb.setSelectedItem(BookStatus.DAMAGED);
        });

        Method clearMethod = LibraryShellFrame.BookPanel.class.getDeclaredMethod("clearForm");
        clearMethod.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try { clearMethod.invoke(panel); } catch (Exception ex) { throw new RuntimeException(ex); }
        });

        assertEquals("", t.getText());
        assertEquals("", av.getText());
        assertEquals(BookStatus.AVAILABLE, sb.getSelectedItem());
    }
}
