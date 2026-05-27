package com.thayhoang.quanly.khanh_book;

import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcBookRepository;
import com.thayhoang.quanly.testsupport.TestDbHelper;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcBookRepositoryMappingTest {

    @BeforeAll
    static void init() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    public void findById_mapsDamagedStatusFromDb() throws SQLException {
        String bookId = "TBK-DMG-1";
        TestDbHelper.deleteBook(bookId);
        TestDbHelper.upsertBook(bookId, "Damaged Book", "Auth", "Pub", 2020, 1, 0, BookStatus.DAMAGED);

        JdbcBookRepository repo = new JdbcBookRepository();

        Book loaded = repo.findById(bookId).orElseThrow();

        assertEquals(BookStatus.DAMAGED, loaded.status());
    }

    @Test
    public void update_persistsDamagedStatus() throws SQLException {
        String bookId = "TBK-DMG-2";
        TestDbHelper.deleteBook(bookId);
        TestDbHelper.upsertBook(bookId, "Before Update", "Auth", "Pub", 2020, 2, 2, BookStatus.AVAILABLE);

        JdbcBookRepository repo = new JdbcBookRepository();
        Book toUpdate = new Book(bookId, "Before Update", "Auth", "Pub", 2020, 2, 0, BookStatus.DAMAGED);

        repo.update(toUpdate);

        String dumped = TestDbHelper.dumpBook(bookId);
        assertTrue(dumped.contains("status=DAMAGED"), "Expected DB row to contain status=DAMAGED but was: " + dumped);
    }
}
