package com.thayhoang.quanly.application.service;

import com.thayhoang.quanly.application.service.dto.LoanRecord;
import java.util.List;
import java.util.Optional;

public interface HistoryService {
    List<LoanRecord> getReaderHistory(String readerId);

    Optional<LoanRecord> getLoanRecord(String loanId);
}
