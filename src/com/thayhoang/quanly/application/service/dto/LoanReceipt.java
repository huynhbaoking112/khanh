package com.thayhoang.quanly.application.service.dto;

import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import java.util.List;

public record LoanReceipt(Loan loan, List<LoanDetail> details) {
}
