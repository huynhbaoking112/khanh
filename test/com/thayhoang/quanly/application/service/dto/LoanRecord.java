package com.thayhoang.quanly.application.service.dto;

import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.model.Librarian;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Fine;
import java.util.List;
import java.util.Optional;

public record LoanRecord(
        Loan loan,
        Reader reader,
        Librarian librarian,
        List<LoanDetail> details,
        Optional<Fine> fine) {
}
