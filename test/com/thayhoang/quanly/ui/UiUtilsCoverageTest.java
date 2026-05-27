package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.model.Librarian;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UiUtilsCoverageTest {

    @Test
    public void parseInt_invalid_throws() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> UiUtils.parseInt("abc", "Nam"));
        assertTrue(ex.getMessage().contains("Nam"));
    }

    @Test
    public void parseDate_invalid_throws() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> UiUtils.parseDate("2026/01/01", "Ngay"));
        assertTrue(ex.getMessage().contains("yyyy-MM-dd"));
    }

    @Test
    public void parseCsv_nullAndBlank_and_tokens() {
        assertTrue(UiUtils.parseCsv(null).isEmpty());
        assertTrue(UiUtils.parseCsv("   ").isEmpty());
        var list = UiUtils.parseCsv(" a, b, ,c ");
        assertEquals(List.of("a","b","c"), list);
    }

    @Test
    public void createBookFromFields_and_fromRow_statusDefault() {
        Book b = UiUtils.createBookFromFields("  id ", " t ", "a", "p", "2020", "5", "3", "");
        assertEquals("id", b.bookId());
        assertEquals(BookStatus.AVAILABLE, b.status());

        Object[] row = new Object[] { null, null, null, null, null, null, null, null };
        Book b2 = UiUtils.createBookFromRow(row);
        assertEquals(0, b2.yearPublish());
        assertEquals(0, b2.quantityTotal());
    }

    @Test
    public void createReaderFromFields_and_fromRow_statusDefault() {
        Reader r = UiUtils.createReaderFromFields(null, "n", "p", "e", "2", "");
        assertEquals(2, r.maxBorrow());
        assertEquals(ReaderStatus.ACTIVE, r.status());

        Object[] row = new Object[] { null, null, null, null, null, null };
        Reader r2 = UiUtils.createReaderFromRow(row);
        assertEquals(0, r2.maxBorrow());
    }

    @Test
    public void formatLoanAndReturnReceipt_variations() {
        String loan = UiUtils.formatLoanReceipt("L1","R1","LIB","2026-06-01",2);
        assertTrue(loan.contains("Ma phieu: L1"));

        String ret = UiUtils.formatReturnReceipt("L1","COMPLETED","2026-06-02","Tien phat: 0");
        assertTrue(ret.contains("Trang thai: COMPLETED"));
    }

    @Test
    public void formatLoanRecordDetails_withFine_and_withoutFine() {
        Loan loan = new Loan("L1","R1","LIB",LocalDate.now(),LocalDate.now().plusDays(7),null, LoanStatus.ACTIVE);
        Reader reader = new Reader("R1","Name","090","e@x",1, ReaderStatus.ACTIVE);
        Librarian lib = new Librarian("LIB","L","u","p", com.thayhoang.quanly.domain.enums.UserRole.LIBRARIAN);
        LoanDetail d = new LoanDetail("LD","L1","B1",1,false,"");
        Fine fine = new Fine("F1","L1", BigDecimal.valueOf(1000), "Late", FinePaymentStatus.UNPAID);
        LoanRecord recWithFine = new LoanRecord(loan, reader, lib, List.of(d), java.util.Optional.of(fine));
        String out = UiUtils.formatLoanRecordDetails(recWithFine);
        assertTrue(out.contains("Phieu muon: L1"));
        assertTrue(out.contains("- B1"));
        assertTrue(out.contains("1000"));

        LoanRecord recNoFine = new LoanRecord(loan, reader, lib, List.of(d), java.util.Optional.empty());
        String out2 = UiUtils.formatLoanRecordDetails(recNoFine);
        assertTrue(out2.contains("Khong co") || out2.contains("Phat:"));
    }
}
