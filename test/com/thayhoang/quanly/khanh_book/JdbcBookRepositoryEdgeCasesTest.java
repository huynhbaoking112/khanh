package com.thayhoang.quanly.khanh_book;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcBookRepository;
import com.thayhoang.quanly.testsupport.TestDbHelper;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcBookRepositoryEdgeCasesTest {
    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("Repository: findById returns empty for non-existent book")
    void findByIdReturnsEmptyForMissing() throws SQLException {
        String bookId = "NON_EXISTENT_BOOK";
        TestDbHelper.deleteBook(bookId);

        JdbcBookRepository repo = new JdbcBookRepository();
        var book = repo.findById(bookId);

        assertFalse(book.isPresent());
    }
}
