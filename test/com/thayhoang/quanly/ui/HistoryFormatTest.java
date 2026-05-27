package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.model.Librarian;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryFormatTest {

    @Test
    public void formatLoanRecordDetails_containsExpectedParts() {
        Loan loan = new Loan("L1", "R1", "LIB1", LocalDate.of(2026,5,1), LocalDate.of(2026,5,15), null, LoanStatus.ACTIVE);
        Reader reader = new Reader("R1", "Reader One", "0901", "a@b.com", 3, null);
        Librarian lib = new Librarian("LIB1", "Librarian One", "user", "pw", UserRole.ADMIN);
        LoanDetail d = new LoanDetail("LD1", "L1", "B1", 1, false, "");
        Fine fine = new Fine("F1", "L1", new BigDecimal(50000), "Late", FinePaymentStatus.UNPAID);
        LoanRecord rec = new LoanRecord(loan, reader, lib, List.of(d), Optional.of(fine));

        String formatted = UiUtils.formatLoanRecordDetails(rec);
        assertTrue(formatted.contains("Phieu muon: L1"));
        assertTrue(formatted.contains("Doc gia: Reader One"));
        assertTrue(formatted.contains("Chi tiet sach:"));
        assertTrue(formatted.contains("B1"));
        assertTrue(formatted.contains("Phat:"));
    }
}
