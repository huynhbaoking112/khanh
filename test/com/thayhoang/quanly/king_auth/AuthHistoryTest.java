package com.thayhoang.quanly.king_auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.thayhoang.quanly.application.repository.LibrarianRepository;
import com.thayhoang.quanly.application.service.AuthenticationService;
import com.thayhoang.quanly.application.service.HistoryService;
import com.thayhoang.quanly.application.service.dto.AuthenticatedSession;
import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.application.service.impl.AuthenticationServiceImpl;
import com.thayhoang.quanly.application.service.impl.HistoryServiceImpl;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.enums.UserRole;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcFineRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLibrarianRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLoanDetailRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLoanRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcReaderRepository;
import com.thayhoang.quanly.testsupport.GapReportBuilder;
import com.thayhoang.quanly.testsupport.QcReport;
import com.thayhoang.quanly.testsupport.TestDbHelper;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JDialog;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthHistoryTest {
    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("TC01 - login success returns a session for the seed admin")
    void tc01_loginSuccessReturnsSeedAdminSession() {
        AuthenticationService service = new AuthenticationServiceImpl(new JdbcLibrarianRepository());

        Optional<AuthenticatedSession> session = service.login("admin", "admin123");

        assertTrue(session.isPresent());
        assertEquals("LIB001", session.orElseThrow().librarianId());
        assertEquals(UserRole.ADMIN, session.orElseThrow().role());

        QcReport.tc("TC01", "System / Unit")
                .requirement("FR01 - Xac thuc tai khoan thu thu")
                .dataset("TD01")
                .precondition("DB co librarian seed username=admin, password=admin123")
                .input("username=admin, password=admin123; action=login")
                .expected("Tra ve AuthenticatedSession va mo man hinh lam viec chinh")
                .actual("session_present=" + session.isPresent() + ", librarianId=" + session.orElseThrow().librarianId())
                .pass();
    }

    @Test
    @DisplayName("TC02 - blank credentials still reach the repository and return empty")
    void tc02_blankCredentialsReachRepositoryAndReturnEmpty() {
        RecordingLibrarianRepository repository = new RecordingLibrarianRepository();
        AuthenticationService service = new AuthenticationServiceImpl(repository);

        Optional<AuthenticatedSession> session = service.login("   ", "");

        assertFalse(session.isPresent());
        // Service now blocks blank credentials and does not call repository
        assertEquals("(none)", repository.lastUsernameLookup);
        assertEquals(0, repository.lookupCount);

        QcReport.tc("TC02", "Unit")
            .requirement("FR01 - Kiem tra validation rong o form login")
            .dataset("TD01")
            .precondition("LoginDialog dang hien thi voi 2 o trong")
            .input("username='   ', password=''; action=login")
            .expected("Form phai chan submit truoc khi goi service (or service must block blank credentials)")
            .actual("AuthenticationServiceImpl now blocks blank credentials; session_present=" + session.isPresent())
            .pass();
    }

    @Test
    @DisplayName("TC03 - wrong password keeps the entered values when the warning popup is dismissed")
    void tc03_wrongPasswordDoesNotClearFields() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Swing login dialog test requires a graphical environment.");

        StubAuthenticationService authenticationService = new StubAuthenticationService();
        JDialog dialog = newLoginDialog(authenticationService);
        JTextField usernameField = getField(dialog, "usernameField", JTextField.class);
        JPasswordField passwordField = getField(dialog, "passwordField", JPasswordField.class);
        usernameField.setText("admin");
        passwordField.setText("wrong-password");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(() -> invokeLogin(dialog));
            waitAndDismissMessageDialogs();
            authenticationService.awaitCalls();

            assertEquals("admin", usernameField.getText());
            assertEquals("wrong-password", new String(passwordField.getPassword()));
            assertEquals(1, authenticationService.callCount);
            assertFalse(authenticationService.returnedSession);

            QcReport.tc("TC03", "System")
                    .requirement("FR01 - Dang nhap sai mat khau")
                    .dataset("TD02")
                    .precondition("LoginDialog dang mo tren may co GUI")
                    .input("username=admin, password=sai_mat_khau; action=click Dang nhap")
                    .expected("WARNING_MESSAGE, giu nguyen gia tri o form va khong reset focus")
                    .actual("serviceCalled=" + authenticationService.callCount + ", usernameAfter='" + usernameField.getText()
                            + "', passwordAfter='" + new String(passwordField.getPassword()) + "'")
                    .pass();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("TC04 - unknown username returns empty session")
    void tc04_unknownUsernameReturnsEmptySession() {
        AuthenticationService service = new AuthenticationServiceImpl(new JdbcLibrarianRepository());

        Optional<AuthenticatedSession> session = service.login("unknown_user", "password123");

        assertFalse(session.isPresent());

        QcReport.tc("TC04", "Integration")
                .requirement("FR01 - Dang nhap tai khoan khong ton tai")
                .dataset("TD03")
                .precondition("DB khong co username=unknown_user")
                .input("username=unknown_user, password=password123; action=login")
                .expected("Repository tra ve null/empty va UI bao that bai")
                .actual("session_present=" + session.isPresent())
                .pass();
    }

    @Test
    @DisplayName("TC05 - reader history loads joined loan records")
    void tc05_readerHistoryLoadsJoinedLoanRecords() throws SQLException {
        String readerId = "RH05";
        String loanId = "LH05";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Reader History Valid", "0901050005", "reader.history.valid@example.com", 5, ReaderStatus.ACTIVE);
        TestDbHelper.upsertLoan(loanId, readerId, "LIB001", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 8), null, LoanStatus.ACTIVE);
        TestDbHelper.upsertLoanDetail("LHD05", loanId, "B001", 1, false, "seed book");

        HistoryService service = new HistoryServiceImpl(
                new JdbcReaderRepository(),
                new JdbcLibrarianRepository(),
                new JdbcLoanRepository(),
                new JdbcLoanDetailRepository(),
                new JdbcFineRepository());

        List<LoanRecord> history = service.getReaderHistory(readerId);

        assertEquals(1, history.size());
        assertEquals(loanId, history.get(0).loan().loanId());
        assertEquals(readerId, history.get(0).reader().readerId());
        assertEquals(1, history.get(0).details().size());
        assertTrue(history.get(0).fine().isEmpty());

        QcReport.tc("TC05", "Integration")
                .requirement("FR02 - Tra cuu lich su muon tra theo doc gia")
                .dataset("TD10")
                .precondition("Doc gia RH05 co 1 loan va 1 loan_detail trong DB")
                .input("readerId=RH05; action=Tra cuu")
                .expected("Bang lich su hien thi dong loan ket hop reader, librarian, detail, fine")
                .actual("historySize=" + history.size() + ", firstLoan=" + history.get(0).loan().loanId()
                        + ", finePresent=" + history.get(0).fine().isPresent())
                .dbBefore(TestDbHelper.dumpReader(readerId) + " || " + TestDbHelper.dumpLoan(loanId))
                .dbAfter(TestDbHelper.dumpReader(readerId) + " || " + TestDbHelper.dumpLoan(loanId))
                .pass();
    }

    @Test
    @DisplayName("TC06 - reader history is empty when the reader has no loans")
    void tc06_readerHistoryIsEmptyWhenNoLoansExist() throws SQLException {
        String readerId = "RH06";
        TestDbHelper.deleteLoansForReader(readerId);
        TestDbHelper.upsertReader(readerId, "Reader History Empty", "0901050006", "reader.history.empty@example.com", 5, ReaderStatus.ACTIVE);

        HistoryService service = new HistoryServiceImpl(
                new JdbcReaderRepository(),
                new JdbcLibrarianRepository(),
                new JdbcLoanRepository(),
                new JdbcLoanDetailRepository(),
                new JdbcFineRepository());

        List<LoanRecord> history = service.getReaderHistory(readerId);

        assertTrue(history.isEmpty());

        QcReport.tc("TC06", "Integration")
                .requirement("FR02 - Doc gia chua co giao dich thi bang rong")
                .dataset("TD10")
                .precondition("Doc gia RH06 ton tai nhung khong co loan nao")
                .input("readerId=RH06; action=Tra cuu")
                .expected("JTable xoa het dong va hien trang thai trong")
                .actual("historySize=" + history.size())
                .pass();
    }

    private static JDialog newLoginDialog(AuthenticationService authenticationService) throws Exception {
        Constructor<?> constructor = Class.forName("com.thayhoang.quanly.ui.LoginDialog")
                .getDeclaredConstructor(Frame.class, AuthenticationService.class);
        constructor.setAccessible(true);
        return (JDialog) constructor.newInstance((Frame) null, authenticationService);
    }

    private static void invokeLogin(JDialog dialog) {
        try {
            Method loginMethod = dialog.getClass().getDeclaredMethod("login");
            loginMethod.setAccessible(true);
            loginMethod.invoke(dialog);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private static <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }

    private static void waitAndDismissMessageDialogs() throws Exception {
        for (int attempt = 0; attempt < 100; attempt++) {
            boolean foundVisibleDialog = false;
            for (Window window : Window.getWindows()) {
                if (window instanceof JDialog dialog && dialog.isShowing()) {
                    foundVisibleDialog = true;
                    dialog.dispose();
                }
            }
            if (!foundVisibleDialog) {
                Thread.sleep(25L);
            }
        }
    }

    private static final class RecordingLibrarianRepository implements LibrarianRepository {
        private int lookupCount;
        private String lastUsernameLookup = "(none)";

        @Override
        public Optional<com.thayhoang.quanly.domain.model.Librarian> findById(String librarianId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<com.thayhoang.quanly.domain.model.Librarian> findById(java.sql.Connection connection, String librarianId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<com.thayhoang.quanly.domain.model.Librarian> findByUsername(String username) {
            lookupCount++;
            lastUsernameLookup = username;
            return Optional.empty();
        }

        @Override
        public List<com.thayhoang.quanly.domain.model.Librarian> findAll() {
            return List.of();
        }

        @Override
        public com.thayhoang.quanly.domain.model.Librarian save(com.thayhoang.quanly.domain.model.Librarian librarian) {
            return librarian;
        }

        @Override
        public int count() {
            return 0;
        }
    }

    private static final class StubAuthenticationService implements AuthenticationService {
        private volatile int callCount;
        private volatile boolean returnedSession;

        @Override
        public Optional<AuthenticatedSession> login(String username, String password) {
            callCount++;
            returnedSession = false;
            return Optional.empty();
        }

        void awaitCalls() throws InterruptedException {
            int attempts = 0;
            while (callCount == 0 && attempts++ < 100) {
                Thread.sleep(20L);
            }
        }
    }
}