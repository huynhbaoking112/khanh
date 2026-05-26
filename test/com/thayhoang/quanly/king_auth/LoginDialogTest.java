package com.thayhoang.quanly.king_auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.thayhoang.quanly.application.service.AuthenticationService;
import com.thayhoang.quanly.ui.LoginDialog;
import java.awt.GraphicsEnvironment;
import javax.swing.JDialog;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginDialogTest {
    @Test
    @DisplayName("LoginDialog blocks blank credentials before calling service")
    void blocksBlankCredentials() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Swing test requires GUI");

        AuthenticationService auth = mock(AuthenticationService.class);

        // create dialog via reflection (constructor is private)
        var ctor = LoginDialog.class.getDeclaredConstructor(java.awt.Frame.class, AuthenticationService.class);
        ctor.setAccessible(true);
        JDialog dialog = (JDialog) ctor.newInstance((java.awt.Frame) null, auth);

        // set fields to blank
        var usernameField = dialog.getClass().getDeclaredField("usernameField");
        usernameField.setAccessible(true);
        ((javax.swing.JTextField) usernameField.get(dialog)).setText("   ");

        var passwordField = dialog.getClass().getDeclaredField("passwordField");
        passwordField.setAccessible(true);
        ((javax.swing.JPasswordField) passwordField.get(dialog)).setText("");

        // invoke private login() method
        var loginMethod = dialog.getClass().getDeclaredMethod("login");
        loginMethod.setAccessible(true);
        loginMethod.invoke(dialog);

        // service.login should not be called
        verifyNoInteractions(auth);
    }
}
