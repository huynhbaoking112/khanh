package com.thayhoang.quanly.thien_circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thayhoang.quanly.application.repository.ReaderRepository;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcReaderRepository;
import com.thayhoang.quanly.testsupport.QcReport;
import com.thayhoang.quanly.testsupport.TestDbHelper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CirculationPerformanceTest {
    private static final int VIOLATING_READER_COUNT = 17;

    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("TC32 - overdue reader retrieval uses one JOIN query for 17 readers")
    void tc32_overdueReaderRetrievalEliminatesNPlusOneQueries() throws SQLException {
        LocalDate today = LocalDate.of(2026, 5, 28);
        for (int index = 1; index <= VIOLATING_READER_COUNT; index++) {
            String suffix = String.format("%02d", index);
            String readerId = "CR32R" + suffix;
            String bookId = "CB32B" + suffix;
            String loanId = "CL32L" + suffix;
            TestDbHelper.deleteLoansForReader(readerId);
            TestDbHelper.upsertReader(readerId, "Circulation Performance Reader " + suffix,
                    "09010932" + suffix, "circulation.performance." + suffix + "@example.com",
                    5, ReaderStatus.ACTIVE);
            TestDbHelper.upsertBook(bookId, "Circulation Performance Book " + suffix,
                    "Pham Hung Thien", "NXB Test", 2024, 1, 0, BookStatus.AVAILABLE);
            TestDbHelper.upsertLoan(loanId, readerId, "LIB001",
                    today.minusDays(20), today.minusDays(6), null, LoanStatus.OVERDUE);
            TestDbHelper.upsertLoanDetail("CL32D" + suffix, loanId, bookId, 1, false,
                    "TC32 overdue active loan");
        }

        CountingReaderRepository readerRepository = new CountingReaderRepository(new JdbcReaderRepository());

        List<Reader> violatingReaders = readerRepository.findOverdueReaders(today);
        long tc32Matches = violatingReaders.stream()
                .filter(reader -> reader.readerId().startsWith("CR32R"))
                .count();

        assertEquals(1, readerRepository.queryCount());
        assertEquals(VIOLATING_READER_COUNT, tc32Matches);
        assertTrue(violatingReaders.stream().allMatch(reader -> reader.status() == ReaderStatus.ACTIVE));

        QcReport.tc("TC32", "Improvement")
                .requirement("FR08 - Loai bo N+1 query khi truy van doc gia vi pham qua han")
                .dataset("TD32 (17 readers CR32R01..CR32R17 + overdue loan/detail rows)")
                .precondition("Seed DB co simulated data cua 17 doc gia voi sach dang muon qua han")
                .input("Query danh sach doc gia vi pham qua han tai today=" + today)
                .expected("Retrieval chay qua 1 cau SQL JOIN duy nhat, DB call count = 1")
                .actual("violatingReaders_TC32=" + tc32Matches
                        + ", SQL query_count=" + readerRepository.queryCount())
                .pass();
    }

    private static final class CountingReaderRepository implements ReaderRepository {
        private final ReaderRepository delegate;
        private int queryCount;

        private CountingReaderRepository(ReaderRepository delegate) {
            this.delegate = delegate;
        }

        int queryCount() {
            return queryCount;
        }

        @Override
        public Optional<Reader> findById(String readerId) throws SQLException {
            return delegate.findById(readerId);
        }

        @Override
        public Optional<Reader> findById(Connection connection, String readerId) throws SQLException {
            return delegate.findById(connection, readerId);
        }

        @Override
        public List<Reader> findAll() throws SQLException {
            return delegate.findAll();
        }

        @Override
        public Reader save(Reader reader) throws SQLException {
            return delegate.save(reader);
        }

        @Override
        public Reader update(Reader reader) throws SQLException {
            return delegate.update(reader);
        }

        @Override
        public int count() throws SQLException {
            return delegate.count();
        }

        @Override
        public List<Reader> findOverdueReaders(LocalDate today) throws SQLException {
            queryCount++;
            return delegate.findOverdueReaders(today);
        }
    }
}
