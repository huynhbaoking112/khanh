package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderPanelErrorHandlingTest {

    @Test
    public void createReader_handlesServiceException() throws Exception {
        ReaderManagementService svc = new ReaderManagementService() {
            @Override public java.util.List<Reader> listReaders() { return List.of(); }
            @Override public java.util.Optional<Reader> getReader(String readerId) { return java.util.Optional.empty(); }
            @Override public Reader createReader(Reader reader) { throw new ApplicationException("fail create"); }
            @Override public Reader updateReader(Reader reader) { return reader; }
            @Override public int getCurrentBorrowCount(String readerId) { return 0; }
        };

        final LibraryShellFrame.ReaderPanel[] panel = new LibraryShellFrame.ReaderPanel[1];
        SwingUtilities.invokeAndWait(() -> panel[0] = new LibraryShellFrame.ReaderPanel(svc, true));

        java.lang.reflect.Method m = panel[0].getClass().getDeclaredMethod("createReader");
        m.setAccessible(true);
        // should not throw because runSafely catches ApplicationException
        SwingUtilities.invokeAndWait(() -> {
            try { m.invoke(panel[0]); } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @Test
    public void updateReader_handlesRuntimeException() throws Exception {
        ReaderManagementService svc = new ReaderManagementService() {
            @Override public java.util.List<Reader> listReaders() { return List.of(); }
            @Override public java.util.Optional<Reader> getReader(String readerId) { return java.util.Optional.empty(); }
            @Override public Reader createReader(Reader reader) { return reader; }
            @Override public Reader updateReader(Reader reader) { throw new RuntimeException("boom"); }
            @Override public int getCurrentBorrowCount(String readerId) { return 0; }
        };

        final LibraryShellFrame.ReaderPanel[] panel = new LibraryShellFrame.ReaderPanel[1];
        SwingUtilities.invokeAndWait(() -> panel[0] = new LibraryShellFrame.ReaderPanel(svc, true));

        java.lang.reflect.Method m = panel[0].getClass().getDeclaredMethod("updateReader");
        m.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try { m.invoke(panel[0]); } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
