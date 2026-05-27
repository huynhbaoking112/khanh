package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class BookPanelValidationBranchesTest {

    private static class FakeBookService implements BookCatalogService {
        @Override public java.util.List<Book> listBooks() { return java.util.Collections.emptyList(); }
        @Override public java.util.List<Book> searchBooks(String keyword, boolean onlyAvailable) { return java.util.Collections.emptyList(); }
        @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
        @Override public Book createBook(Book book) { throw new UnsupportedOperationException(); }
        @Override public Book updateBook(Book book) { throw new UnsupportedOperationException(); }
    }

    @Test
    public void readBookFromForm_throws_onMissingTitle_orInvalidNumbers() throws Exception {
        FakeBookService svc = new FakeBookService();
        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(svc, true);

        // set fields with invalid/empty title
        var titleF = LibraryShellFrame.BookPanel.class.getDeclaredField("titleField");
        titleF.setAccessible(true);
        titleF.set(panel, new JTextField(""));

        var yearF = LibraryShellFrame.BookPanel.class.getDeclaredField("yearField");
        yearF.setAccessible(true);
        yearF.set(panel, new JTextField("not-a-number"));

        var totalF = LibraryShellFrame.BookPanel.class.getDeclaredField("totalField");
        totalF.setAccessible(true);
        totalF.set(panel, new JTextField("-5"));

        var availF = LibraryShellFrame.BookPanel.class.getDeclaredField("availableField");
        availF.setAccessible(true);
        availF.set(panel, new JTextField("-1"));

        var statusF = LibraryShellFrame.BookPanel.class.getDeclaredField("statusBox");
        statusF.setAccessible(true);
        JComboBox<BookStatus> box = new JComboBox<>(BookStatus.values());
        box.setSelectedItem(BookStatus.AVAILABLE);
        statusF.set(panel, box);

        var method = LibraryShellFrame.BookPanel.class.getDeclaredMethod("readBookFromForm");
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(panel));
    }
}
