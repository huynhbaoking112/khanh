package com.thayhoang.quanly.khoa_reader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thayhoang.quanly.application.repository.LoanDetailRepository;
import com.thayhoang.quanly.application.service.impl.ReaderManagementServiceImpl;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLoanDetailRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcReaderRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Vu Dinh Khoa - Reader Management unit tests (TC15, TC16).
// These tests hit the real SQLite DB at data/library.db so the teacher can verify evidence
// via `sqlite3 data/library.db` after the run.
class ReaderManagementRulesTest {
    private final JdbcReaderRepository readerRepository = new JdbcReaderRepository();
    private final LoanDetailRepository loanDetailRepository = new JdbcLoanDetailRepository();
    private final ReaderManagementServiceImpl service =
            new ReaderManagementServiceImpl(readerRepository, loanDetailRepository);

    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("TC15 - changing reader status from ACTIVE to locked persists the new status (REAL DB)")
    void tc15_updateReaderStatusToLockedPersistsNewState() throws SQLException {
        // Setup: ensure R015 exists with ACTIVE status before the test runs.
        TestDbHelper.upsertReader("R015", "Reader Status Lock", "0901000015",
                "reader.status.lock@example.com", 5, ReaderStatus.ACTIVE);
        String before = TestDbHelper.dumpReader("R015");

        // Action: librarian flips status to INACTIVE (= LOCKED per the V6 spec) and hits Save.
        Reader lockedReader = new Reader("R015", "Reader Status Lock", "0901000015",
                "reader.status.lock@example.com", 5, ReaderStatus.INACTIVE);
        Reader updated = service.updateReader(lockedReader);

        // Verify: read R015 back from the real DB and confirm status persisted.
        String after = TestDbHelper.dumpReader("R015");
        Reader fromDb = readerRepository.findById("R015").orElseThrow();
        assertEquals(ReaderStatus.INACTIVE, updated.status());
        assertEquals(ReaderStatus.INACTIVE, fromDb.status());

        // Auto-detect: does ReaderStatus enum actually have LOCKED constant?
        boolean hasLocked = GapDetector.hasReaderStatusLocked();
        String enumNames = GapDetector.describeReaderStatusEnum();
        String statusUsedByCode = lockedReader.status().name();
        String statusInDbAfterUpdate = fromDb.status().name();

        QcReport report = QcReport.tc("TC15", "Unit Testing")
                .requirement("FR07 - Chinh sua trang thai the doc gia")
                .dataset("TD11")
                .precondition("DB co reader R015 voi status=ACTIVE (vua duoc upsert tu Setup)")
                .input("readerId=R015, statusMoi=" + statusUsedByCode
                        + " (= LOCKED theo tai lieu); action=bam nut Luu")
                .expected("UPDATE READER SET status='" + statusUsedByCode
                        + "' WHERE reader_id='R015'; SELECT lai phai tra ve status=" + statusUsedByCode)
                .actual("service.updateReader() goi update() thanh cong; "
                        + "SELECT R015 tu DB tra ve status=" + statusInDbAfterUpdate)
                .dbBefore(before)
                .dbAfter(after);

        if (hasLocked) {
            report.pass();
        } else {
            report.gap(GapReportBuilder.evidence("ENUM EVIDENCE")
                    .field("ReaderStatus.values()", enumNames)
                    .field("spec_required_constant", "'LOCKED'")
                    .field("constant_present_in_code", hasLocked)
                    .field("status_used_by_test", statusUsedByCode)
                    .field("status_persisted_in_db", statusInDbAfterUpdate)
                    .conclusion("Spec yeu cau enum 'LOCKED' nhung ma nguon dung '" + statusUsedByCode
                            + "' => naming mismatch giua tai lieu va code.")
                    .fixSuggestion("Bo sung enum LOCKED vao ReaderStatus.java HOAC cap nhat tai lieu "
                            + "thong nhat ten " + statusUsedByCode + ".")
                    .severity("LOW (chi la naming convention)")
                    .build());
        }
    }

    @Test
    @DisplayName("TC16 - reader update validation documents current phone-field gap (REAL DB)")
    void tc16_emptyReaderPhoneCurrentlyPassesValidation() throws SQLException {
        // Setup: insert R016 with valid phone so we can compare BEFORE/AFTER.
        String originalPhone = "0901000016";
        TestDbHelper.upsertReader("R016", "Reader With Phone", originalPhone,
                "reader.with.phone@example.com", 3, ReaderStatus.ACTIVE);
        String before = TestDbHelper.dumpReader("R016");
        Reader readerBeforeUpdate = readerRepository.findById("R016").orElseThrow();

        // Action: librarian wipes the phone field and hits Update.
        Reader readerWithBlankPhone = new Reader("R016", "Reader With Phone", "",
                "reader.with.phone@example.com", 3, ReaderStatus.ACTIVE);

        // Capture whether the service throws (it should, per spec, but currently does not).
        boolean serviceThrew;
        String exceptionName;
        try {
            service.updateReader(readerWithBlankPhone);
            serviceThrew = false;
            exceptionName = "(none)";
        } catch (RuntimeException ex) {
            serviceThrew = true;
            exceptionName = ex.getClass().getSimpleName() + " - " + ex.getMessage();
        }

        // Verify: phone is now blank in the DB - this is the gap (spec says it should have been rejected).
        String after = TestDbHelper.dumpReader("R016");
        Reader fromDb = readerRepository.findById("R016").orElseThrow();
        String persistedPhone = fromDb.phone() == null ? "null" : "'" + fromDb.phone() + "'";
        boolean persistedPhoneIsBlank = fromDb.phone() == null || fromDb.phone().isEmpty();
        assertTrue(persistedPhoneIsBlank,
                "Expected blank phone to be persisted in current implementation (evidence of gap)");

        QcReport.tc("TC16", "Unit Testing")
                .requirement("FR07 - Validate so dien thoai bat buoc")
                .dataset("TD12")
                .precondition("DB co reader R016 (" + readerBeforeUpdate.fullName()
                        + ") voi phone='" + readerBeforeUpdate.phone() + "' (valid)")
                .input("readerId=R016, fullName=Reader With Phone, phone=\"\" (rong), "
                        + "email=reader.with.phone@example.com; action=bam nut Cap nhat")
                .expected("Service phai NEM BusinessRuleViolationException va khong duoc UPDATE DB "
                        + "(theo tai lieu V6, phone la truong bat buoc)")
                .actual("service.updateReader() exception_thrown=" + serviceThrew
                        + " (" + exceptionName + "); "
                        + "SELECT R016 sau update -> phone=" + persistedPhone
                        + ", is_blank=" + persistedPhoneIsBlank)
                .dbBefore(before)
                .dbAfter(after)
                .gap(GapReportBuilder.evidence("VALIDATION EVIDENCE")
                        .field("phone_before", "'" + readerBeforeUpdate.phone() + "'")
                        .field("phone_input_to_service", "''")
                        .field("service_threw_exception", serviceThrew)
                        .field("observed_exception", exceptionName)
                        .field("phone_persisted_in_db", persistedPhone)
                        .field("phone_is_blank_after_update", persistedPhoneIsBlank)
                        .conclusion("Spec yeu cau service NEM exception khi phone rong, "
                                + "NHUNG observed=khong nem va DB van persist phone rong => "
                                + "validation bi bypass tai tang Service.")
                        .fixSuggestion("Them `if(isBlank(reader.phone())) throw new "
                                + "BusinessRuleViolationException(\"So dien thoai bat buoc\");` "
                                + "vao ReaderManagementServiceImpl.validate().")
                        .severity("MEDIUM (validation gap)")
                        .build());
    }
}
