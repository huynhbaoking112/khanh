package com.thayhoang.quanly.ui;

import org.junit.jupiter.api.Test;
import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;

import static org.junit.jupiter.api.Assertions.*;

public class UiUtilsInvalidStatusTest {

    @Test
    public void createBookFromFields_invalidStatus_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> UiUtils.createBookFromFields("id","t","a","p","2020","1","1","NOT_A_STATUS"));
        assertNotNull(ex.getMessage());
    }

    @Test
    public void createReaderFromFields_invalidStatus_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> UiUtils.createReaderFromFields("id","n","p","e","1","NOPE"));
        assertNotNull(ex.getMessage());
    }
}
