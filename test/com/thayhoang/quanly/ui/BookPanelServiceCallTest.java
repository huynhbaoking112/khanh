package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.domain.model.Book;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookPanelServiceCallTest {

    private static class SpyService implements BookCatalogService {
        boolean listCalled = false;
        boolean searchCalled = false;

        @Override public List<Book> listBooks() { listCalled = true; return List.of(); }
        @Override public List<Book> searchBooks(String keyword, boolean onlyAvailable) { searchCalled = true; return List.of(); }
        @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
        @Override public Book createBook(Book book) { throw new UnsupportedOperationException(); }
        @Override public Book updateBook(Book book) { throw new UnsupportedOperationException(); }
    }

    @Test
    public void refreshTable_calls_list_or_search_based_onFields() throws Exception {
        SpyService svc = new SpyService();
        LibraryShellFrame.BookPanel panel = new LibraryShellFrame.BookPanel(svc, true);

        Method refresh = LibraryShellFrame.BookPanel.class.getDeclaredMethod("refreshTable");
        refresh.setAccessible(true);

        // default: empty search, available unchecked -> listBooks
        SwingUtilities.invokeAndWait(() -> {});
        refresh.invoke(panel);
        assertTrue(svc.listCalled);

        // with search text -> searchBooks
        Field searchField = LibraryShellFrame.BookPanel.class.getDeclaredField("searchField");
        searchField.setAccessible(true);
        JTextField sf = (JTextField) searchField.get(panel);
        SwingUtilities.invokeAndWait(() -> sf.setText("kw"));
        refresh.invoke(panel);
        assertTrue(svc.searchCalled);

        // with availableOnly true -> searchBooks
        Field avail = LibraryShellFrame.BookPanel.class.getDeclaredField("availableOnlyCheck");
        avail.setAccessible(true);
        JCheckBox cb = (JCheckBox) avail.get(panel);
        SwingUtilities.invokeAndWait(() -> { sf.setText(""); cb.setSelected(true); });
        refresh.invoke(panel);
        assertTrue(svc.searchCalled);
    }
}
