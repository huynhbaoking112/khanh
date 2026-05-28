package com.thayhoang.quanly.domain.rules;

import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class CirculationRules {
    public static final int DEFAULT_LOAN_DAYS = 14;
    public static final BigDecimal FINE_PER_DAY = BigDecimal.valueOf(50000L);

    private CirculationRules() {
    }

    public static void validateReaderForBorrowing(Reader reader, int currentBorrowCount, int requestedCount) {
        if (reader.status() != ReaderStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Doc gia khong o trang thai hoat dong");
        }
        if (currentBorrowCount + requestedCount > reader.maxBorrow()) {
            throw new BusinessRuleViolationException("Doc gia da vuot gioi han muon sach");
        }
    }

    public static void validateBookAvailability(Book book, int requestedQuantity) {
        if (book.status() != BookStatus.AVAILABLE) {
            throw new BusinessRuleViolationException("Sach hien khong san sang de muon");
        }
        if (book.quantityAvailable() < requestedQuantity) {
            throw new BusinessRuleViolationException("Sach khong con du so luong kha dung");
        }
    }

    public static LocalDate defaultDueDate(LocalDate loanDate) {
        return loanDate.plusDays(DEFAULT_LOAN_DAYS);
    }

    public static BigDecimal calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate == null || !returnDate.isAfter(dueDate)) {
            return BigDecimal.ZERO;
        }

        long lateDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        return FINE_PER_DAY.multiply(BigDecimal.valueOf(lateDays));
    }

    public static void validateLoanCanAcceptReturns(Loan loan) {
        if (loan.status() == LoanStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Phieu muon da hoan tat");
        }
    }

    public static void validateLoanDetailCanBeReturned(LoanDetail loanDetail) {
        if (loanDetail.returned()) {
            throw new BusinessRuleViolationException("Sach nay da duoc tra truoc do");
        }
    }

    public static LoanStatus resolveLoanStatusAfterReturn(boolean allReturned, LocalDate dueDate, LocalDate returnDate) {
        if (allReturned) {
            return LoanStatus.COMPLETED;
        }
        if (returnDate.isAfter(dueDate)) {
            return LoanStatus.OVERDUE;
        }
        return LoanStatus.PARTIALLY_RETURNED;
    }

    public static void validateLoanRenewal(
            Loan loan,
            List<LoanDetail> details,
            int extraDays,
            LocalDate referenceDate) {
        if (extraDays <= 0) {
            throw new BusinessRuleViolationException("So ngay gia han phai lon hon 0");
        }
        if (loan.status() == LoanStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Khong the gia han phieu muon da hoan tat");
        }
        if (loan.status() == LoanStatus.OVERDUE || loan.dueDate().isBefore(referenceDate)) {
            throw new BusinessRuleViolationException("Không được phép gia hạn phiếu mượn đã quá hạn trả sách!");
        }
        boolean hasUnreturnedBook = details.stream().anyMatch(detail -> !detail.returned());
        if (!hasUnreturnedBook) {
            throw new BusinessRuleViolationException("Khong con sach dang muon de gia han");
        }
    }
}
