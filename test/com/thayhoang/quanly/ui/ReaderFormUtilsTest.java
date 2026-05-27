package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.domain.model.Reader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderFormUtilsTest {

    @Test
    public void createReaderFromFields_valid() {
        Reader r = UiUtils.createReaderFromFields("R100", "Nguyen Van A", "0901000000", "a@example.com", "3", "ACTIVE");
        assertEquals("R100", r.readerId());
        assertEquals("Nguyen Van A", r.fullName());
        assertEquals(3, r.maxBorrow());
        assertEquals("ACTIVE", r.status().name());
    }

    @Test
    public void createReaderFromFields_invalidMax() {
        ApplicationException ex = assertThrows(ApplicationException.class, () ->
                UiUtils.createReaderFromFields("R101", "Name", "", "", "not-number", "ACTIVE")
        );
        assertTrue(ex.getMessage().contains("Gioi han muon"));
    }
}
