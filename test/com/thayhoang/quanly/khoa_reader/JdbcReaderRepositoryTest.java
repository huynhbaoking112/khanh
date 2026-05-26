package com.thayhoang.quanly.khoa_reader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcReaderRepository;
import com.thayhoang.quanly.testsupport.TestDbHelper;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcReaderRepositoryTest {
    private final JdbcReaderRepository repo = new JdbcReaderRepository();

    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("findOverdueReaders returns only readers with active overdue loans")
    void findsOverdueReaders() throws SQLException {
        // Setup test readers and loans (RTX)
        TestDbHelper.upsertReader("JRR1", "Overdue Reader", "0901007001", "overdue@example.com", 5, com.thayhoang.quanly.domain.enums.ReaderStatus.ACTIVE);
        TestDbHelper.upsertReader("JRR2", "OnTime Reader", "0901007002", "ontime@example.com", 5, com.thayhoang.quanly.domain.enums.ReaderStatus.ACTIVE);
        TestDbHelper.upsertLoan("JRL1", "JRR1", "LIB001", LocalDate.of(2026,5,1), LocalDate.of(2026,5,8), null, com.thayhoang.quanly.domain.enums.LoanStatus.ACTIVE);
        TestDbHelper.upsertLoan("JRL2", "JRR2", "LIB001", LocalDate.of(2026,5,20), LocalDate.of(2026,5,27), null, com.thayhoang.quanly.domain.enums.LoanStatus.ACTIVE);

        List<Reader> overdue = repo.findOverdueReaders(LocalDate.of(2026,5,23));

        assertTrue(overdue.stream().anyMatch(r -> "JRR1".equals(r.readerId())));
        assertFalse(overdue.stream().anyMatch(r -> "JRR2".equals(r.readerId())));
    }
}
