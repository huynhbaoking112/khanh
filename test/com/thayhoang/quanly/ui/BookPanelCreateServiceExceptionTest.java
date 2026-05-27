package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.domain.model.Book;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookPanelCreateServiceExceptionTest {

    @Test
    public void createBook_handlesServiceApplicationException_and_doesNotPropagate() throws Exception {
        final boolean[] called = {false};
        BookCatalogService fake = new BookCatalogService() {
            @Override public List<Book> listBooks() { return List.of(); }
            @Override public List<Book> searchBooks(String keyword, boolean onlyAvailable) { return List.of(); }
            @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
            @Override public Book createBook(Book b) {
                called[0] = true;
                throw new ApplicationException("service failed");
            }
            @Override public Book updateBook(Book b) { throw new UnsupportedOperationException(); }
        };

        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(fake, true);

        // set minimal valid fields
        Field title = LibraryShellFrame.BookPanel.class.getDeclaredField("titleField");
        Field year = LibraryShellFrame.BookPanel.class.getDeclaredField("yearField");
        Field total = LibraryShellFrame.BookPanel.class.getDeclaredField("totalField");
        Field available = LibraryShellFrame.BookPanel.class.getDeclaredField("availableField");
        title.setAccessible(true); year.setAccessible(true); total.setAccessible(true); available.setAccessible(true);

        JTextField t = (JTextField) title.get(panel);
        JTextField y = (JTextField) year.get(panel);
        JTextField tot = (JTextField) total.get(panel);
        JTextField av = (JTextField) available.get(panel);

        SwingUtilities.invokeAndWait(() -> {
            t.setText("T"); y.setText("2021"); tot.setText("1"); av.setText("1");
        });

        Method create = LibraryShellFrame.BookPanel.class.getDeclaredMethod("createBook");
        create.setAccessible(true);

        // should not throw; runSafely swallows ApplicationException
        SwingUtilities.invokeAndWait(() -> {
            try { create.invoke(panel); } catch (Exception ex) { throw new RuntimeException(ex); }
        });

        assertTrue(called[0]);
    }
}
