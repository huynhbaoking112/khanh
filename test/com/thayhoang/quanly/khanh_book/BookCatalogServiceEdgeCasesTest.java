package com.thayhoang.quanly.khanh_book;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.application.service.impl.BookCatalogServiceImpl;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcBookRepository;
import com.thayhoang.quanly.testsupport.TestDbHelper;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookCatalogServiceEdgeCasesTest {
    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("Service: negative quantity is rejected")
    void negativeQuantityIsRejected() {
        BookCatalogService service = new BookCatalogServiceImpl(new JdbcBookRepository());
        Book invalid = new Book("BK_NEG", "Neg Qty", "Author", "NXB", 2022, -1, -1, BookStatus.AVAILABLE);

        assertThrows(BusinessRuleViolationException.class, () -> service.createBook(invalid));
    }

    @Test
    @DisplayName("Service: available greater than total is rejected")
    void availableGreaterThanTotalIsRejected() {
        BookCatalogService service = new BookCatalogServiceImpl(new JdbcBookRepository());
        Book invalid = new Book("BK_BAD", "Bad Qty", "Author", "NXB", 2022, 1, 2, BookStatus.AVAILABLE);

        assertThrows(BusinessRuleViolationException.class, () -> service.createBook(invalid));
    }
}
