package com.recipeplatform.gui;

import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.InvalidLoginException;
import com.recipeplatform.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// User Registration Screen
public class RegisterFrame extends JFrame {

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    private final LoginFrame parentLoginFrame;
    private final AuthenticationService authService;

    public RegisterFrame(LoginFrame loginFrame) {
        this.parentLoginFrame = loginFrame;
        this.authService = new AuthenticationService();
        initUI();
    }

    private void initUI() {
        setTitle("Register - Recipe Sharing Platform");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(460, 520);
        setLocationRelativeTo(parentLoginFrame);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header
        JLabel headerLabel = new JLabel("Create New Account", SwingConstants.CENTER);
        headerLabel.setFont(UITheme.FONT_TITLE);
        headerLabel.setForeground(UITheme.PRIMARY_COLOR);
        headerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(UITheme.FONT_BOLD);
        formPanel.add(nameLabel, gbc);

        gbc.gridy = 1;
        nameField = new JTextField(20);
        nameField.setFont(UITheme.FONT_REGULAR);
        formPanel.add(nameField, gbc);

        // Email
        gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setFont(UITheme.FONT_BOLD);
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 3;
        emailField = new JTextField(20);
        emailField.setFont(UITheme.FONT_REGULAR);
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridy = 4;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UITheme.FONT_BOLD);
        formPanel.add(passLabel, gbc);

        gbc.gridy = 5;
        passwordField = new JPasswordField(20);
        passwordField.setFont(UITheme.FONT_REGULAR);
        formPanel.add(passwordField, gbc);

        // Role
        gbc.gridy = 6;
        JLabel roleLabel = new JLabel("Select Account Role:");
        roleLabel.setFont(UITheme.FONT_BOLD);
        formPanel.add(roleLabel, gbc);

        gbc.gridy = 7;
        roleComboBox = new JComboBox<>(new String[]{"Explorer", "Contributor", "Admin"});
        roleComboBox.setFont(UITheme.FONT_REGULAR);
        formPanel.add(roleComboBox, gbc);

        // Buttons
        gbc.gridy = 8; gbc.insets = new Insets(18, 5, 5, 5);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);

        JButton submitBtn = UITheme.createPrimaryButton("Register");
        JButton backBtn = UITheme.createSecondaryButton("Back to Login");

        buttonPanel.add(submitBtn);
        buttonPanel.add(backBtn);
        formPanel.add(buttonPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Action Listeners
        submitBtn.addActionListener(e -> performRegistration());
        backBtn.addActionListener(e -> {
            this.dispose();
            parentLoginFrame.setVisible(true);
        });
    }

    private void performRegistration() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        try {
            boolean success = authService.register(name, email, password, role);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Registration successful! You can now log in.",
                        "Account Created",
                        JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                parentLoginFrame.setVisible(true);
            }
        } catch (InvalidLoginException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
