package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookPanelReadFormTest {

    private static class FakeBookService implements BookCatalogService {
        final List<Book> store = new ArrayList<>();

        @Override
        public List<Book> listBooks() { return new ArrayList<>(store); }

        @Override
        public List<Book> searchBooks(String keyword, boolean onlyAvailable) { return listBooks(); }

        @Override
        public java.util.Optional<Book> getBook(String bookId) { return store.stream().filter(b -> b.bookId().equals(bookId)).findFirst(); }

        @Override
        public Book createBook(Book book) { store.add(book); return book; }

        @Override
        public Book updateBook(Book book) { return createBook(book); }
    }

    @Test
    public void readBookFromForm_parsesValuesCorrectly() throws Exception {
        FakeBookService svc = new FakeBookService();
        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(svc, true);

        // set form fields via reflection
        var idFieldF = LibraryShellFrame.BookPanel.class.getDeclaredField("idField");
        idFieldF.setAccessible(true);
        idFieldF.set(panel, new JTextField("BKX01"));

        var titleF = LibraryShellFrame.BookPanel.class.getDeclaredField("titleField");
        titleF.setAccessible(true);
        titleF.set(panel, new JTextField("My Title"));

        var authorF = LibraryShellFrame.BookPanel.class.getDeclaredField("authorField");
        authorF.setAccessible(true);
        authorF.set(panel, new JTextField("An Author"));

        var publisherF = LibraryShellFrame.BookPanel.class.getDeclaredField("publisherField");
        publisherF.setAccessible(true);
        publisherF.set(panel, new JTextField("PubHouse"));

        var yearF = LibraryShellFrame.BookPanel.class.getDeclaredField("yearField");
        yearF.setAccessible(true);
        yearF.set(panel, new JTextField("2022"));

        var totalF = LibraryShellFrame.BookPanel.class.getDeclaredField("totalField");
        totalF.setAccessible(true);
        totalF.set(panel, new JTextField("5"));

        var availF = LibraryShellFrame.BookPanel.class.getDeclaredField("availableField");
        availF.setAccessible(true);
        availF.set(panel, new JTextField("5"));

        var statusF = LibraryShellFrame.BookPanel.class.getDeclaredField("statusBox");
        statusF.setAccessible(true);
        JComboBox<BookStatus> box = new JComboBox<>(BookStatus.values());
        box.setSelectedItem(BookStatus.AVAILABLE);
        statusF.set(panel, box);

        var method = LibraryShellFrame.BookPanel.class.getDeclaredMethod("readBookFromForm");
        method.setAccessible(true);
        Book b = (Book) method.invoke(panel);

        assertEquals("BKX01", b.bookId());
        assertEquals("My Title", b.title());
        assertEquals("An Author", b.author());
        assertEquals("PubHouse", b.publisher());
        assertEquals(2022, b.yearPublish());
        assertEquals(5, b.quantityTotal());
        assertEquals(5, b.quantityAvailable());
        assertEquals(BookStatus.AVAILABLE, b.status());
    }
}
