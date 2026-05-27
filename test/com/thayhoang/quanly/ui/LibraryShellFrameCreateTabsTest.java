package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.bootstrap.ApplicationBootstrap;
import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.application.service.CirculationService;
import com.thayhoang.quanly.application.service.HistoryService;
import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.application.service.dto.AuthenticatedSession;
import com.thayhoang.quanly.domain.enums.UserRole;
import com.thayhoang.quanly.domain.model.Book;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryShellFrameCreateTabsTest {

    static final BookCatalogService BOOK_SERVICE = new BookCatalogService() {
        @Override public List<Book> listBooks() { return List.of(); }
        @Override public List<Book> searchBooks(String keyword, boolean onlyAvailable) { return List.of(); }
        @Override public java.util.Optional<Book> getBook(String bookId) { return java.util.Optional.empty(); }
        @Override public Book createBook(Book book) { return book; }
        @Override public Book updateBook(Book book) { return book; }
    };

    static final ReaderManagementService READER_SERVICE = new ReaderManagementService() {
        @Override public List<com.thayhoang.quanly.domain.model.Reader> listReaders() { return List.of(); }
        @Override public java.util.Optional<com.thayhoang.quanly.domain.model.Reader> getReader(String readerId) { return java.util.Optional.empty(); }
        @Override public com.thayhoang.quanly.domain.model.Reader createReader(com.thayhoang.quanly.domain.model.Reader reader) { return reader; }
        @Override public com.thayhoang.quanly.domain.model.Reader updateReader(com.thayhoang.quanly.domain.model.Reader reader) { return reader; }
        @Override public int getCurrentBorrowCount(String readerId) { return 0; }
    };

    static final CirculationService CIRC_SERVICE = new CirculationService() {
        @Override public com.thayhoang.quanly.application.service.dto.LoanReceipt createLoan(String readerId, String librarianId, List<String> bookIds) { return null; }
        @Override public com.thayhoang.quanly.application.service.dto.ReturnReceipt returnBooks(String loanId, List<String> returnedBookIds, java.time.LocalDate returnDate) { return null; }
        @Override public com.thayhoang.quanly.domain.model.Loan renewLoan(String loanId, int extraDays, java.time.LocalDate referenceDate) { return null; }
    };

    static final HistoryService HISTORY_SERVICE = new HistoryService() {
        @Override public List<com.thayhoang.quanly.application.service.dto.LoanRecord> getReaderHistory(String readerId) { return List.of(); }
        @Override public java.util.Optional<com.thayhoang.quanly.application.service.dto.LoanRecord> getLoanRecord(String loanId) { return java.util.Optional.empty(); }
    };

    private ApplicationBootstrap bootstrap = new ApplicationBootstrap(
            Path.of("/tmp/db"),
            null,
            BOOK_SERVICE,
            READER_SERVICE,
            CIRC_SERVICE,
            HISTORY_SERVICE);

    @Test
    public void createTabs_adminAndNonAdmin_hasFourTabs_and_bookPanelLoadSelectedNoSelection() throws Exception {
        // admin session
        AuthenticatedSession admin = new AuthenticatedSession("LIB1", "Admin User", "admin", UserRole.ADMIN);
        final LibraryShellFrame[] adminFrame = new LibraryShellFrame[1];
        SwingUtilities.invokeAndWait(() -> adminFrame[0] = new LibraryShellFrame(bootstrap, admin));

        JTabbedPane tabsAdmin = findTabbedPane(adminFrame[0]);
        assertNotNull(tabsAdmin);
        assertEquals(4, tabsAdmin.getTabCount());

        // non-admin session
        AuthenticatedSession user = new AuthenticatedSession("LIB2", "Normal", "user", UserRole.LIBRARIAN);
        final LibraryShellFrame[] userFrame = new LibraryShellFrame[1];
        SwingUtilities.invokeAndWait(() -> userFrame[0] = new LibraryShellFrame(bootstrap, user));

        JTabbedPane tabsUser = findTabbedPane(userFrame[0]);
        assertNotNull(tabsUser);
        assertEquals(4, tabsUser.getTabCount());

        // exercise BookPanel.loadSelectedBook negative branch by invoking private method
        Object bookPanel = tabsUser.getComponentAt(0);
        assertNotNull(bookPanel);
        // call private method loadSelectedBook - should simply return when no selection
        java.lang.reflect.Method m = bookPanel.getClass().getDeclaredMethod("loadSelectedBook");
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try {
                m.invoke(bookPanel);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private JTabbedPane findTabbedPane(LibraryShellFrame frame) {
        for (java.awt.Component c : frame.getContentPane().getComponents()) {
            if (c instanceof JTabbedPane) return (JTabbedPane) c;
            if (c instanceof java.awt.Container) {
                for (java.awt.Component cc : ((java.awt.Container) c).getComponents()) {
                    if (cc instanceof JTabbedPane) return (JTabbedPane) cc;
                }
            }
        }
        return null;
    }
}
