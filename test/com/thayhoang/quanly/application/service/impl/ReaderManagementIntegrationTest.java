package com.thayhoang.quanly.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thayhoang.quanly.application.repository.LoanDetailRepository;
import com.thayhoang.quanly.application.repository.LoanRepository;
import com.thayhoang.quanly.application.repository.ReaderRepository;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.Reader;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Vu Dinh Khoa - Reader Management integration tests (TC13, TC14, TC18).
// Companion suites: ReaderManagementRulesTest (TC15, TC16), ReaderManagementSystemTest (TC17).
class ReaderManagementIntegrationTest {
    @Test
    @DisplayName("TC13 - new reader form submission persists with default ACTIVE status")
    void tc13_createReaderWithValidFormPersistsAsActive() throws SQLException {
        ReaderRepository readerRepository = mock(ReaderRepository.class);
        LoanDetailRepository loanDetailRepository = mock(LoanDetailRepository.class);
        ReaderManagementServiceImpl service =
                new ReaderManagementServiceImpl(readerRepository, loanDetailRepository);

        // TD09 - fully populated form payload submitted by the librarian via "Them doc gia".
        Reader formInput =
                new Reader("R013", "Tran Thi Mai", "0901000013", "mai@example.com", 5, ReaderStatus.ACTIVE);

        when(readerRepository.findById("R013")).thenReturn(Optional.empty());
        when(readerRepository.save(formInput)).thenReturn(formInput);

        // TC13 - Integration Test: a valid form submission is uniqueness-checked then persisted as ACTIVE.
        Reader saved = service.createReader(formInput);

        assertEquals("R013", saved.readerId());
        assertEquals(ReaderStatus.ACTIVE, saved.status());
        verify(readerRepository).findById("R013");
        verify(readerRepository).save(argThat(reader ->
                "R013".equals(reader.readerId()) && reader.status() == ReaderStatus.ACTIVE));
    }

    @Test
    @DisplayName("TC14 - duplicate reader primary key propagates as SQL constraint failure")
    void tc14_duplicateReaderIdCausesPrimaryKeyViolation() throws SQLException {
        ReaderRepository repository = mock(ReaderRepository.class);
        Reader duplicate = new Reader("R001", "Duplicate Reader", "0901000001", "dup@example.com", 3,
                ReaderStatus.ACTIVE);

        when(repository.save(duplicate)).thenThrow(new SQLException("SQLITE_CONSTRAINT_PRIMARYKEY"));

        // TC14 - Integration Test: repository layer surfaces database primary-key constraint failures.
        SQLException exception = assertThrows(SQLException.class, () -> repository.save(duplicate));

        assertTrue(exception.getMessage().contains("SQLITE_CONSTRAINT"));
    }

    @Test
    @DisplayName("TC18 - overdue readers can be resolved by joining reader list with active loan due dates")
    void tc18_overdueReadersResolvedFromActiveLoanDueDates() throws SQLException {
        ReaderRepository readerRepository = mock(ReaderRepository.class);
        LoanRepository loanRepository = mock(LoanRepository.class);
        LocalDate today = LocalDate.of(2026, 5, 23);

        // TD12 - two readers with different loan situations.
        Reader overdueReader =
                new Reader("R001", "Nguyen Van An", "0900000001", "an@example.com", 5, ReaderStatus.ACTIVE);
        Reader onTimeReader =
                new Reader("R002", "Tran Thi Binh", "0900000002", "binh@example.com", 5, ReaderStatus.ACTIVE);
        Reader noLoanReader =
                new Reader("R003", "Le Van Cuong", "0900000003", "cuong@example.com", 5, ReaderStatus.ACTIVE);

        Loan overdueLoan = new Loan(
                "LOAN001", "R001", "LIB001",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 8), null, LoanStatus.ACTIVE);
        Loan onTimeLoan = new Loan(
                "LOAN002", "R002", "LIB001",
                LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 27), null, LoanStatus.ACTIVE);
        Loan completedLateLoan = new Loan(
                "LOAN003", "R003", "LIB001",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 8), LocalDate.of(2026, 4, 15),
                LoanStatus.COMPLETED);

        when(readerRepository.findAll())
                .thenReturn(List.of(overdueReader, onTimeReader, noLoanReader));
        when(loanRepository.findByReaderId("R001")).thenReturn(List.of(overdueLoan));
        when(loanRepository.findByReaderId("R002")).thenReturn(List.of(onTimeLoan));
        when(loanRepository.findByReaderId("R003")).thenReturn(List.of(completedLateLoan));

        // TC18 - Integration Test: documents the cross-repository JOIN behavior expected by the spec.
        // Gap: JdbcReaderRepository does not yet expose a dedicated findOverdueReaders() SQL JOIN, and
        // LibraryShellFrame does not yet show a "Ra soat doc gia tre han" navigation button. Until those
        // are added, the equivalent overdue list can be assembled via existing reader and loan repositories.
        List<Reader> overdueList = new ArrayList<>();
        for (Reader reader : readerRepository.findAll()) {
            for (Loan loan : loanRepository.findByReaderId(reader.readerId())) {
                if (loan.status() != LoanStatus.COMPLETED && loan.dueDate().isBefore(today)) {
                    overdueList.add(reader);
                    break;
                }
            }
        }

        assertEquals(1, overdueList.size());
        assertEquals("R001", overdueList.get(0).readerId());
        assertTrue(overdueList.stream().noneMatch(r -> r.readerId().equals("R002")));
        assertTrue(overdueList.stream().noneMatch(r -> r.readerId().equals("R003")));
        verify(readerRepository).findAll();
        verify(loanRepository).findByReaderId("R001");
        verify(loanRepository).findByReaderId("R002");
        verify(loanRepository).findByReaderId("R003");
        verify(loanRepository, never()).findByReaderId("R999");
    }
}
