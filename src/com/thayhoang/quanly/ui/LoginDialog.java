package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.service.AuthenticationService;
import com.thayhoang.quanly.application.service.dto.AuthenticatedSession;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class LoginDialog extends JDialog {
    private final AuthenticationService authenticationService;
    private final JTextField usernameField = new JTextField("admin", 18);
    private final JPasswordField passwordField = new JPasswordField("admin123", 18);
    private AuthenticatedSession session;

    private LoginDialog(Frame owner, AuthenticationService authenticationService) {
        super(owner, "Dang Nhap", true);
        this.authenticationService = authenticationService;
        buildUi();
    }

    public static Optional<AuthenticatedSession> showDialog(Frame owner, AuthenticationService authenticationService) {
        LoginDialog dialog = new LoginDialog(owner, authenticationService);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.session);
    }

    private void buildUi() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 0, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tai khoan"), gbc);

        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Mat khau"), gbc);

        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton(new AbstractAction("Dang nhap") {
            @Override
            public void actionPerformed(ActionEvent event) {
                login();
            }
        });
        JButton cancelButton = new JButton(new AbstractAction("Thoat") {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        getRootPane().setDefaultButton(loginButton);
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private void login() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isBlank() || pass.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui long nhap tai khoan va mat khau.",
                    "Dang nhap that bai",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Optional<AuthenticatedSession> authenticated = authenticationService.login(user, pass);
        if (authenticated.isPresent()) {
            session = authenticated.get();
            dispose();
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Tai khoan hoac mat khau khong dung.",
                "Dang nhap that bai",
                JOptionPane.WARNING_MESSAGE);
    }
}
