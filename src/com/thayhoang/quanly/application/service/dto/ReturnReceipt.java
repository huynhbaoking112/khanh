package com.thayhoang.quanly.application.service.dto;

import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import java.util.List;
import java.util.Optional;

public record ReturnReceipt(Loan loan, List<LoanDetail> details, Optional<Fine> fine) {
}
