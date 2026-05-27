package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.exception.ApplicationException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UiUtilsTest {

    @Test
    public void parseInt_validAndInvalid() {
        assertEquals(42, UiUtils.parseInt("42", "Field"));
        ApplicationException ex = assertThrows(ApplicationException.class, () -> UiUtils.parseInt("x", "Field"));
        assertTrue(ex.getMessage().contains("khong hop le"));
    }

    @Test
    public void parseDate_validAndInvalid() {
        LocalDate d = UiUtils.parseDate("2026-05-27", "Ngay");
        assertEquals(2026, d.getYear());
        ApplicationException ex = assertThrows(ApplicationException.class, () -> UiUtils.parseDate("bad", "Ngay"));
        assertTrue(ex.getMessage().contains("phai theo dinh dang"));
    }

    @Test
    public void parseCsv_splitsAndTrims() {
        List<String> parts = UiUtils.parseCsv("a, b, ,c");
        assertEquals(3, parts.size());
        assertEquals("a", parts.get(0));
        assertEquals("b", parts.get(1));
        assertEquals("c", parts.get(2));
    }

    @Test
    public void formatLoanAndReturnReceipt_containsFields() {
        String loan = UiUtils.formatLoanReceipt("L1", "R1", "LIB", "2026-06-03", 2);
        assertTrue(loan.contains("Ma phieu: L1"));

        String ret = UiUtils.formatReturnReceipt("L1", "COMPLETED", "2026-06-04", "Tien phat: 1000");
        assertTrue(ret.contains("Trang thai: COMPLETED"));
    }
}
