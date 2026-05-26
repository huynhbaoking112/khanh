package com.thayhoang.quanly;

import com.thayhoang.quanly.application.bootstrap.ApplicationBootstrap;
import com.thayhoang.quanly.application.service.dto.AuthenticatedSession;
import com.thayhoang.quanly.ui.LoginDialog;
import com.thayhoang.quanly.ui.LibraryShellFrame;
import java.awt.GraphicsEnvironment;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            try {
                ApplicationBootstrap.initialize();
            } catch (Exception exception) {
                handleStartupFailure(exception);
            }
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                ApplicationBootstrap bootstrap = ApplicationBootstrap.initialize();
                Optional<AuthenticatedSession> session = LoginDialog.showDialog(null, bootstrap.authenticationService());
                if (session.isEmpty()) {
                    return;
                }
                LibraryShellFrame frame = new LibraryShellFrame(bootstrap, session.get());
                frame.setVisible(true);
            } catch (Exception exception) {
                handleStartupFailure(exception);
            }
        });
    }

    private static void handleStartupFailure(Exception exception) {
        exception.printStackTrace();

        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Khoi dong ung dung that bai:\n" + exception.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
