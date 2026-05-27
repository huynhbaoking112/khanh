package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Librarian;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.application.service.dto.LoanRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UiUtilsExtraTest {

    @Test
    public void createBookAndReader_fromFields_nullStatus_usesDefault() {
        Book b = UiUtils.createBookFromFields("id","t","a","p","2020","5","5", null);
        assertEquals(BookStatus.AVAILABLE, b.status());

        Reader r = UiUtils.createReaderFromFields("rid","name","090","e@e","3", "");
        assertEquals(ReaderStatus.ACTIVE, r.status());
    }

    @Test
    public void createBookFromRow_handlesNulls() {
        Object[] row = new Object[] {null, null, null, null, null, null, null, null};
        Book b = UiUtils.createBookFromRow(row);
        assertEquals("", b.bookId());
        assertEquals(BookStatus.AVAILABLE, b.status());
    }

    @Test
    public void formatLoanRecordDetails_includesDetails_and_fine() {
        Loan loan = new Loan("L1","R1","LIB", LocalDate.of(2026,5,1), LocalDate.of(2026,5,15), null, com.thayhoang.quanly.domain.enums.LoanStatus.ACTIVE);
        Reader reader = new Reader("R1","Reader Name","090","e@e",3, com.thayhoang.quanly.domain.enums.ReaderStatus.ACTIVE);
        Librarian librarian = new Librarian("LIB","Lib Name","user","pw", com.thayhoang.quanly.domain.enums.UserRole.LIBRARIAN);
        LoanDetail d = new LoanDetail("D1","L1","B1",1,false, "");
        Fine fine = new Fine("F1","L1", BigDecimal.valueOf(50000), "late", com.thayhoang.quanly.domain.enums.FinePaymentStatus.UNPAID);
        LoanRecord record = new LoanRecord(loan, reader, librarian, List.of(d), Optional.of(fine));

        String out = UiUtils.formatLoanRecordDetails(record);
        assertTrue(out.contains("Phieu muon: L1"));
        assertTrue(out.contains("- B1"));
        assertTrue(out.contains("Phat:") || out.contains("Khong co"));
    }
}
