package com.thayhoang.quanly.domain.model;

import com.thayhoang.quanly.domain.enums.LoanStatus;
import java.time.LocalDate;

public record Loan(
        String loanId,
        String readerId,
        String librarianId,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        LoanStatus status) {
}
