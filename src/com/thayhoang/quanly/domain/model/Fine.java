package com.thayhoang.quanly.domain.model;

import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import java.math.BigDecimal;

public record Fine(
        String fineId,
        String loanId,
        BigDecimal amount,
        String reason,
        FinePaymentStatus paidStatus) {
}
