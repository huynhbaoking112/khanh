package com.thayhoang.quanly.domain.model;

public record LoanDetail(
        String loanDetailId,
        String loanId,
        String bookId,
        int quantity,
        boolean returned,
        String note) {
}
