package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.domain.model.Book;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookFormUtilsTest {

    @Test
    public void createBookFromFields_valid() {
        Book b = UiUtils.createBookFromFields("BK100", "Title", "Author", "Pub", "2022", "5", "3", "AVAILABLE");
        assertEquals("BK100", b.bookId());
        assertEquals("Title", b.title());
        assertEquals(2022, b.yearPublish());
        assertEquals(5, b.quantityTotal());
        assertEquals(3, b.quantityAvailable());
        assertEquals("AVAILABLE", b.status().name());
    }

    @Test
    public void createBookFromFields_invalidNumber() {
        ApplicationException ex = assertThrows(ApplicationException.class, () ->
                UiUtils.createBookFromFields("BK101", "T", "A", "P", "not-a-year", "x", "y", "AVAILABLE")
        );
        assertTrue(ex.getMessage().contains("Nam xuat ban") || ex.getMessage().contains("So luong"));
    }
}
