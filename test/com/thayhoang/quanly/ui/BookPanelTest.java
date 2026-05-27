package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookPanelTest {

    private static class FakeBookService implements BookCatalogService {
        final List<Book> store = new ArrayList<>();

        @Override
        public List<Book> listBooks() {
            return new ArrayList<>(store);
        }

        @Override
        public List<Book> searchBooks(String keyword, boolean availableOnly) {
            return listBooks();
        }

        @Override
        public java.util.Optional<Book> getBook(String bookId) { return store.stream().filter(b -> b.bookId().equals(bookId)).findFirst(); }

        @Override
        public Book createBook(Book book) {
            store.add(book);
            return book;
        }

        @Override
        public Book updateBook(Book book) {
            for (int i = 0; i < store.size(); i++) {
                if (store.get(i).bookId().equals(book.bookId())) {
                    store.set(i, book);
                    return book;
                }
            }
            store.add(book);
            return book;
        }
    }

    @Test
    public void refreshAndCreate() throws Exception {
        FakeBookService svc = new FakeBookService();
        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(svc, true);

        // initial model empty
        JSplitPane split = (JSplitPane) panel.getComponent(1);
        JScrollPane left = (JScrollPane) split.getLeftComponent();
        JTable table = (JTable) left.getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        assertEquals(0, model.getRowCount());

        // create a book via service and refresh
        svc.createBook(new Book("B100", "T", "A", "P", 2020, 2, 2, BookStatus.AVAILABLE));
        // call refreshTable via reflection
        var m = LibraryShellFrame.BookPanel.class.getDeclaredMethod("refreshTable");
        m.setAccessible(true);
        m.invoke(panel);
        assertEquals(1, model.getRowCount());

        // fill form fields and call createBook
        var idFieldF = LibraryShellFrame.BookPanel.class.getDeclaredField("idField");
        idFieldF.setAccessible(true);
        idFieldF.set(panel, new JTextField("B200"));
        var titleFieldF = LibraryShellFrame.BookPanel.class.getDeclaredField("titleField");
        titleFieldF.setAccessible(true);
        titleFieldF.set(panel, new JTextField("New Book"));
        var authorFieldF = LibraryShellFrame.BookPanel.class.getDeclaredField("authorField");
        authorFieldF.setAccessible(true);
        authorFieldF.set(panel, new JTextField("Auth"));
        var publisherFieldF = LibraryShellFrame.BookPanel.class.getDeclaredField("publisherField");
        publisherFieldF.setAccessible(true);
        publisherFieldF.set(panel, new JTextField("Pub"));
        var yearFieldF = LibraryShellFrame.BookPanel.class.getDeclaredField("yearField");
        yearFieldF.setAccessible(true);
        yearFieldF.set(panel, new JTextField("2021"));
        var totalFieldF = LibraryShellFrame.BookPanel.class.getDeclaredField("totalField");
        totalFieldF.setAccessible(true);
        totalFieldF.set(panel, new JTextField("3"));
        var availableFieldF = LibraryShellFrame.BookPanel.class.getDeclaredField("availableField");
        availableFieldF.setAccessible(true);
        availableFieldF.set(panel, new JTextField("3"));
        var statusBoxF = LibraryShellFrame.BookPanel.class.getDeclaredField("statusBox");
        statusBoxF.setAccessible(true);
        statusBoxF.set(panel, new JComboBox<>(BookStatus.values()));

        var createMethod = LibraryShellFrame.BookPanel.class.getDeclaredMethod("createBook");
        createMethod.setAccessible(true);
        createMethod.invoke(panel);

        // service should have new book
        assertTrue(svc.store.stream().anyMatch(b -> b.bookId().equals("B200")));
    }
}
