package com.thayhoang.quanly.khanh_book;

import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.application.service.impl.BookCatalogServiceImpl;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcBookRepository;
import com.thayhoang.quanly.testsupport.TestDbHelper;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookCatalogServiceEdgeBranchesTest {

    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    public void createBook_duplicateId_throwsBusinessRule() throws SQLException {
        String bookId = "BK-DUP-1";
        TestDbHelper.deleteBook(bookId);
        TestDbHelper.upsertBook(bookId, "Existing", "Auth", "Pub", 2020, 1, 1, BookStatus.AVAILABLE);

        BookCatalogService svc = new BookCatalogServiceImpl(new JdbcBookRepository());
        Book newBook = new Book(bookId, "Existing", "Auth", "Pub", 2020, 1, 1, BookStatus.AVAILABLE);

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () -> svc.createBook(newBook));
        assertEquals("Ma sach da ton tai", ex.getMessage());
    }

    @Test
    public void updateBook_notFound_throwsBusinessRule() throws SQLException {
        String bookId = "BK-NOTFOUND-1";
        TestDbHelper.deleteBook(bookId);

        BookCatalogService svc = new BookCatalogServiceImpl(new JdbcBookRepository());
        Book toUpdate = new Book(bookId, "Nope", "Auth", "Pub", 2020, 1, 1, BookStatus.AVAILABLE);

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () -> svc.updateBook(toUpdate));
        assertEquals("Khong tim thay sach can cap nhat", ex.getMessage());
    }

    @Test
    public void updateBook_quantityBoundary_allowsAvailableEqualTotal() throws SQLException {
        String bookId = "BK-QB-1";
        TestDbHelper.deleteBook(bookId);
        TestDbHelper.upsertBook(bookId, "Boundary", "Auth", "Pub", 2020, 5, 5, BookStatus.AVAILABLE);

        BookCatalogService svc = new BookCatalogServiceImpl(new JdbcBookRepository());
        Book updated = new Book(bookId, "Boundary", "Auth", "Pub", 2020, 5, 5, BookStatus.AVAILABLE);

        Book result = svc.updateBook(updated);
        assertEquals(5, result.quantityAvailable());
    }
}
