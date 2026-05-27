package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.domain.model.Reader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UiRowConversionTest {

    @Test
    public void createBookFromRow_convertsCorrectly() {
        Object[] row = new Object[] {"B1", "Title", "Auth", "Pub", 2021, 10, 5, "AVAILABLE"};
        Book b = UiUtils.createBookFromRow(row);
        assertEquals("B1", b.bookId());
        assertEquals(2021, b.yearPublish());
        assertEquals(10, b.quantityTotal());
        assertEquals(5, b.quantityAvailable());
    }

    @Test
    public void createReaderFromRow_convertsCorrectly() {
        Object[] row = new Object[] {"R1", "Reader A", "0901", "a@x.com", 2, "ACTIVE", 0};
        Reader r = UiUtils.createReaderFromRow(row);
        assertEquals("R1", r.readerId());
        assertEquals("Reader A", r.fullName());
        assertEquals(2, r.maxBorrow());
        assertEquals("ACTIVE", r.status().name());
    }
}
