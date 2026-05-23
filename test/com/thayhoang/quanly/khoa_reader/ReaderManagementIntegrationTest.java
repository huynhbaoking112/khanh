package com.thayhoang.quanly.khoa_reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.service.impl.ReaderManagementServiceImpl;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLoanDetailRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLoanRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcReaderRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Vu Dinh Khoa - Reader Management integration tests (TC13, TC14, TC18).
// All test data uses self-descriptive names ("Reader Create Valid", "Reader With Phone", ...)
// so they are easy to distinguish from real reader rows when inspecting data/library.db.
class ReaderManagementIntegrationTest {
    private final JdbcReaderRepository readerRepository = new JdbcReaderRepository();
    private final JdbcLoanRepository loanRepository = new JdbcLoanRepository();
    private final JdbcLoanDetailRepository loanDetailRepository = new JdbcLoanDetailRepository();
    private final ReaderManagementServiceImpl service =
            new ReaderManagementServiceImpl(readerRepository, loanDetailRepository);

    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("TC13 - new reader form submission persists into the real DB with ACTIVE status")
    void tc13_createReaderWithValidFormPersistsAsActive() throws SQLException {
        // Setup: ensure R013 does NOT exist so we can prove the INSERT happened.
        TestDbHelper.deleteReader("R013");
        String before = TestDbHelper.dumpReader("R013");

        // Action: submit the "Them moi" form with self-descriptive test data.
        Reader formInput = new Reader("R013", "Reader Create Valid", "0901000013",
                "reader.create.valid@example.com", 5, ReaderStatus.ACTIVE);
        Reader saved = service.createReader(formInput);

        // Verify: row exists in DB with ACTIVE status.
        String after = TestDbHelper.dumpReader("R013");
        Reader fromDb = readerRepository.findById("R013").orElseThrow();
        assertEquals("R013", saved.readerId());
        assertEquals(ReaderStatus.ACTIVE, fromDb.status());
        assertEquals("Reader Create Valid", fromDb.fullName());

        QcReport.tc("TC13", "Integration Testing")
                .requirement("FR06 - Khoi tao moi ho so doc gia")
                .dataset("TD09")
                .precondition("DB chua co readerId=R013 (Setup vua DELETE)")
                .input("readerId=R013, fullName=Reader Create Valid, phone=0901000013, "
                        + "email=reader.create.valid@example.com, maxBorrow=5, status=ACTIVE; "
                        + "action=bam nut Them moi")
                .expected("Sau INSERT: SELECT R013 tra ve 1 row voi fullName='Reader Create Valid' "
                        + "va status=ACTIVE")
                .actual("INSERT thanh cong; SELECT R013 tra ve: " + fromDb.fullName()
                        + ", status=" + fromDb.status().name())
                .dbBefore(before)
                .dbAfter(after)
                .pass();
    }

    @Test
    @DisplayName("TC14 - duplicate reader primary key is rejected by the service layer (REAL DB)")
    void tc14_duplicateReaderIdCausesRejection() throws SQLException {
        // Setup: ensure RT14 exists as the existing reader that we will try to duplicate.
        TestDbHelper.upsertReader("RT14", "Reader Duplicate Target", "0901014014",
                "reader.duplicate.target@example.com", 3, ReaderStatus.ACTIVE);
        String before = TestDbHelper.dumpReader("RT14");

        // Action: try to create another reader with the same ID RT14 but different payload.
        Reader duplicate = new Reader("RT14", "Reader Duplicate Attempt", "0901999999",
                "reader.duplicate.attempt@example.com", 3, ReaderStatus.ACTIVE);
        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class, () -> service.createReader(duplicate));

        // Verify: RT14 in DB is unchanged after the rejected attempt.
        String after = TestDbHelper.dumpReader("RT14");
        Reader fromDb = readerRepository.findById("RT14").orElseThrow();
        assertEquals("Ma doc gia da ton tai", exception.getMessage());
        assertFalse("Reader Duplicate Attempt".equals(fromDb.fullName()),
                "Duplicate INSERT must NOT overwrite the existing RT14");

        QcReport.tc("TC14", "Integration Testing")
                .requirement("FR06 - Rang buoc khoa chinh ma doc gia")
                .dataset("TD10")
                .precondition("DB da co reader RT14 (Reader Duplicate Target, vua upsert)")
                .input("readerId=RT14 (TRUNG), fullName=Reader Duplicate Attempt, phone=0901999999, "
                        + "email=reader.duplicate.attempt@example.com; "
                        + "action=goi service.createReader(duplicate)")
                .expected("Service NEM BusinessRuleViolationException(\"Ma doc gia da ton tai\"); "
                        + "row RT14 trong DB GIU NGUYEN 'Reader Duplicate Target', khong bi ghi de")
                .actual("Exception ne'm: " + exception.getMessage()
                        + "; RT14 trong DB van la: " + fromDb.fullName())
                .dbBefore(before)
                .dbAfter(after)
                .pass();
    }

    @Test
    @DisplayName("TC18 - overdue readers query resolves over real LOAN and READER rows")
    void tc18_overdueReadersResolvedFromRealLoanData() throws SQLException {
        LocalDate today = LocalDate.of(2026, 5, 23);

        // Setup: 3 dedicated test readers (RT18A/B/C) so seed readers R001/R002/R003 stay untouched.
        TestDbHelper.upsertReader("RT18A", "Reader Overdue Case", "0901018001",
                "reader.overdue@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertReader("RT18B", "Reader OnTime Case", "0901018002",
                "reader.ontime@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertReader("RT18C", "Reader Completed Case", "0901018003",
                "reader.completed@example.com", 5, ReaderStatus.ACTIVE);

        TestDbHelper.upsertLoan("LT18A", "RT18A", "LIB001",
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 8), null, LoanStatus.ACTIVE);
        TestDbHelper.upsertLoan("LT18B", "RT18B", "LIB001",
                LocalDate.of(2026, 5, 20), LocalDate.of(2026, 5, 27), null, LoanStatus.ACTIVE);
        TestDbHelper.upsertLoan("LT18C", "RT18C", "LIB001",
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 8), LocalDate.of(2026, 4, 15),
                LoanStatus.COMPLETED);

        String beforeLoans = String.join(" || ",
                TestDbHelper.dumpLoan("LT18A"),
                TestDbHelper.dumpLoan("LT18B"),
                TestDbHelper.dumpLoan("LT18C"));

        // Action: cross-repository JOIN to find overdue readers (simulates the spec'd UI button behavior).
        // Track real query counts so the GAP report can quantify the N+1 access pattern.
        int findAllCalls = 0;
        int findByReaderIdCalls = 0;
        List<Reader> overdueList = new ArrayList<>();
        List<Reader> allReaders = readerRepository.findAll();
        findAllCalls++;
        for (Reader reader : allReaders) {
            List<Loan> readerLoans = loanRepository.findByReaderId(reader.readerId());
            findByReaderIdCalls++;
            for (Loan loan : readerLoans) {
                if (loan.status() != LoanStatus.COMPLETED && loan.dueDate().isBefore(today)) {
                    overdueList.add(reader);
                    break;
                }
            }
        }
        int totalQueries = findAllCalls + findByReaderIdCalls;

        // Verify: only RT18A should appear in the overdue list among our test readers.
        assertTrue(overdueList.stream().anyMatch(r -> "RT18A".equals(r.readerId())),
                "RT18A must appear in overdue list (loan LT18A dueDate < today)");
        assertFalse(overdueList.stream().anyMatch(r -> "RT18B".equals(r.readerId())),
                "RT18B must NOT appear (loan LT18B still on time)");
        assertFalse(overdueList.stream().anyMatch(r -> "RT18C".equals(r.readerId())),
                "RT18C must NOT appear (loan LT18C already COMPLETED)");

        String afterLoans = String.join(" || ",
                TestDbHelper.dumpLoan("LT18A"),
                TestDbHelper.dumpLoan("LT18B"),
                TestDbHelper.dumpLoan("LT18C"));

        // Auto-detect: does JdbcReaderRepository have any *overdue* SQL method declared?
        boolean hasOverdueMethod = GapDetector.hasFindOverdueReadersMethod();
        String methodsList = GapDetector.describeReaderRepositoryMethods();

        QcReport report = QcReport.tc("TC18", "Integration Testing")
                .requirement("FR08 - Truy van doc gia vi pham qua han")
                .dataset("TD12 (3 reader RT18A/B/C + 3 loan LT18A/B/C thuc te trong DB)")
                .precondition("DB co 3 test reader (RT18A=Overdue, RT18B=OnTime, RT18C=Completed) "
                        + "va 3 loan LT18A/B/C vua upsert. Ngay tham chieu = 2026-05-23")
                .input("readerRepository.findAll() + loanRepository.findByReaderId(...) cho moi reader; "
                        + "loc loan voi status != COMPLETED va dueDate < today")
                .expected("Danh sach overdue chua RT18A (loan LT18A dueDate=2026-05-08 < today), "
                        + "KHONG chua RT18B (con han) va RT18C (da COMPLETED)")
                .actual("Tim duoc " + overdueList.size() + " overdue reader(s); "
                        + "RT18A trong list = "
                        + overdueList.stream().anyMatch(r -> "RT18A".equals(r.readerId()))
                        + "; RT18B trong list = "
                        + overdueList.stream().anyMatch(r -> "RT18B".equals(r.readerId()))
                        + "; RT18C trong list = "
                        + overdueList.stream().anyMatch(r -> "RT18C".equals(r.readerId())))
                .dbBefore("LOANs vua setup: " + beforeLoans)
                .dbAfter("LOANs sau test (khong doi vi chi query): " + afterLoans);

        if (hasOverdueMethod) {
            report.pass();
        } else {
            report.gap(GapReportBuilder.evidence("REPO METHOD + QUERY COUNT EVIDENCE")
                    .field("JdbcReaderRepository.getDeclaredMethods()", methodsList)
                    .field("methods_containing_'overdue'", hasOverdueMethod ? 1 : 0)
                    .field("readers_iterated", allReaders.size())
                    .field("findAll_calls", findAllCalls)
                    .field("findByReaderId_calls", findByReaderIdCalls)
                    .field("total_DB_round_trips", totalQueries
                            + " (= " + findAllCalls + " findAll + " + findByReaderIdCalls + " findByReaderId)")
                    .conclusion("Khong co method 'overdue' chuyen biet trong JdbcReaderRepository => "
                            + "test phai gop tu " + totalQueries + " queries thay vi 1 SQL JOIN duy nhat. "
                            + "N+1 access pattern xuat hien khi so reader tang.")
                    .fixSuggestion("Bo sung `findOverdueReaders(LocalDate today)` voi 1 SQL JOIN truc tiep "
                            + "vao JdbcReaderRepository (READER JOIN LOAN WHERE due_date < ? AND status != 'COMPLETED').")
                    .severity("MEDIUM (chuc nang chay duoc nhung khong toi uu)")
                    .build());
        }
    }
}
