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
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CirculationRulesTest {

    @Test
    public void validateReaderForBorrowing_checksStatusAndLimit() {
        Reader inactive = new Reader("R1", "Name", "", "", 3, ReaderStatus.INACTIVE);
        assertThrows(BusinessRuleViolationException.class, () -> CirculationRules.validateReaderForBorrowing(inactive, 0, 1));

        Reader r = new Reader("R2", "Name", "", "", 1, ReaderStatus.ACTIVE);
        assertThrows(BusinessRuleViolationException.class, () -> CirculationRules.validateReaderForBorrowing(r, 1, 1));
    }

    @Test
    public void validateBookAvailability_checksStatusAndQuantity() {
        Book notAvailable = new Book("B1", "T", "A", "P", 2020, 1, 1, BookStatus.INACTIVE);
        assertThrows(BusinessRuleViolationException.class, () -> CirculationRules.validateBookAvailability(notAvailable, 1));

        Book insufficient = new Book("B2", "T", "A", "P", 2020, 5, 0, BookStatus.AVAILABLE);
        assertThrows(BusinessRuleViolationException.class, () -> CirculationRules.validateBookAvailability(insufficient, 1));
    }

    @Test
    public void calculateFine_returnsZeroOrPositive() {
        LocalDate due = LocalDate.of(2026, 5, 1);
        assertEquals(BigDecimal.ZERO, CirculationRules.calculateFine(due, LocalDate.of(2026, 5, 1)));
        assertEquals(BigDecimal.ZERO, CirculationRules.calculateFine(due, null));
        assertTrue(CirculationRules.calculateFine(due, LocalDate.of(2026, 5, 3)).compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    public void resolveLoanStatusAfterReturn_various() {
        assertEquals(LoanStatus.COMPLETED, CirculationRules.resolveLoanStatusAfterReturn(true, LocalDate.now(), LocalDate.now()));
        assertEquals(LoanStatus.OVERDUE, CirculationRules.resolveLoanStatusAfterReturn(false, LocalDate.now().minusDays(2), LocalDate.now()));
        assertEquals(LoanStatus.PARTIALLY_RETURNED, CirculationRules.resolveLoanStatusAfterReturn(false, LocalDate.now().plusDays(1), LocalDate.now()));
    }

    @Test
    public void validateLoanRenewal_variousChecks() {
        Loan loan = new Loan("L1", "R1", "LIB", LocalDate.now(), LocalDate.now().plusDays(7), null, LoanStatus.ACTIVE);
        LoanDetail returned = new LoanDetail("D1", "L1", "B1", 1, true, null);
        LoanDetail notReturned = new LoanDetail("D2", "L1", "B2", 1, false, null);

        assertThrows(BusinessRuleViolationException.class, () -> CirculationRules.validateLoanRenewal(loan, List.of(notReturned), 0, LocalDate.now()));

        Loan completedLoan = new Loan("L2", "R1", "LIB", LocalDate.now(), LocalDate.now().plusDays(7), null, LoanStatus.COMPLETED);
        assertThrows(BusinessRuleViolationException.class, () -> CirculationRules.validateLoanRenewal(completedLoan, List.of(notReturned), 1, LocalDate.now()));

        Loan overdueLoan = new Loan("L3", "R1", "LIB", LocalDate.now().minusDays(10), LocalDate.now().minusDays(3), null, LoanStatus.ACTIVE);
        assertThrows(BusinessRuleViolationException.class, () -> CirculationRules.validateLoanRenewal(overdueLoan, List.of(notReturned), 1, LocalDate.now()));

        assertThrows(BusinessRuleViolationException.class, () -> CirculationRules.validateLoanRenewal(loan, List.of(returned), 1, LocalDate.now()));
    }
}
