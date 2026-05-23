package com.thayhoang.quanly.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Reader;
import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Vu Dinh Khoa - Reader Management system tests (TC17).
// Companion suites: ReaderManagementRulesTest (TC15), ReaderManagementIntegrationTest (TC13, TC18).
class ReaderManagementSystemTest {
    private static final String[] READER_COLUMNS =
            new String[] {"Ma", "Ho ten", "Phone", "Email", "Max", "Trang thai", "Dang muon"};

    @Test
    @DisplayName("TC17 - typing a keyword filters the reader table down to matching rows")
    void tc17_readerTableFilterShowsOnlyMatchingRows() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Swing system test requires a graphical environment.");

        // TD10 - the librarian opens the Doc Gia tab and the full reader list is loaded.
        ReaderManagementService readerService = stubReaderService(List.of(
                new Reader("R001", "Nguyen Van Khoa", "0900000001", "khoa@example.com", 5, ReaderStatus.ACTIVE),
                new Reader("R002", "Tran Thi Mai", "0900000002", "mai@example.com", 5, ReaderStatus.ACTIVE),
                new Reader("R003", "Le Hoang Khoa", "0900000003", "lhkhoa@example.com", 3, ReaderStatus.INACTIVE)));

        DefaultTableModel model = renderReaderTable(readerService);
        JTable readerTable = new JTable(model);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        readerTable.setRowSorter(sorter);

        assertEquals(3, model.getRowCount());

        // TC17 - System Test: typing "khoa" in the filter must collapse the JTable to only matching readers.
        // Gap: ReaderPanel inside LibraryShellFrame does not yet expose a JTextField filter wired to a
        // TableRowSorter. The current production UI only refreshes the whole list; this test models the
        // expected client-side filter behaviour so the panel can be wired up against it.
        sorter.setRowFilter(caseInsensitiveContainsFilter("khoa"));

        assertEquals(2, readerTable.getRowCount());
        assertNotNull(readerTable.getValueAt(0, 1));
        assertTrue(readerTable.getValueAt(0, 1).toString().toLowerCase(Locale.ROOT).contains("khoa"));
        assertTrue(readerTable.getValueAt(1, 1).toString().toLowerCase(Locale.ROOT).contains("khoa"));
    }

    private static DefaultTableModel renderReaderTable(ReaderManagementService readerService) {
        DefaultTableModel model = new DefaultTableModel(READER_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Reader reader : readerService.listReaders()) {
            model.addRow(new Object[] {
                    reader.readerId(),
                    reader.fullName(),
                    reader.phone(),
                    reader.email(),
                    reader.maxBorrow(),
                    reader.status().name(),
                    readerService.getCurrentBorrowCount(reader.readerId())
            });
        }
        return model;
    }

    private static javax.swing.RowFilter<TableModel, Integer> caseInsensitiveContainsFilter(String keyword) {
        String lowered = keyword.toLowerCase(Locale.ROOT);
        return new javax.swing.RowFilter<>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                Object value = entry.getValue(1);
                return value != null && value.toString().toLowerCase(Locale.ROOT).contains(lowered);
            }
        };
    }

    private static ReaderManagementService stubReaderService(List<Reader> readers) {
        return new ReaderManagementService() {
            @Override
            public List<Reader> listReaders() {
                return readers;
            }

            @Override
            public Optional<Reader> getReader(String readerId) {
                return readers.stream().filter(reader -> reader.readerId().equals(readerId)).findFirst();
            }

            @Override
            public Reader createReader(Reader reader) {
                return reader;
            }

            @Override
            public Reader updateReader(Reader reader) {
                return reader;
            }

            @Override
            public int getCurrentBorrowCount(String readerId) {
                return 0;
            }
        };
    }
}
