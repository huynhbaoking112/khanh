package com.thayhoang.quanly.application.service;

import com.thayhoang.quanly.application.service.dto.LoanReceipt;
import com.thayhoang.quanly.application.service.dto.ReturnReceipt;
import com.thayhoang.quanly.domain.model.Loan;
import java.time.LocalDate;
import java.util.List;

public interface CirculationService {
    LoanReceipt createLoan(String readerId, String librarianId, List<String> bookIds);

    ReturnReceipt returnBooks(String loanId, List<String> returnedBookIds, LocalDate returnDate);

    Loan renewLoan(String loanId, int extraDays, LocalDate referenceDate);
}
