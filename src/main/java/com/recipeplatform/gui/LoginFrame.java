package com.recipeplatform.gui;

import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.InvalidLoginException;
import com.recipeplatform.model.User;
import com.recipeplatform.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Main Login Screen demonstrating User Authentication and Polymorphic Dashboard launching
public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private final AuthenticationService authService;

    public LoginFrame() {
        this.authService = new AuthenticationService();
        initUI();
    }

    private void initUI() {
        setTitle("Online Recipe Sharing Platform - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Online Recipe Sharing Platform");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("BTech Java GUI College Project (Review 1)");
        subtitleLabel.setFont(UITheme.FONT_SMALL);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Card Panel
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(UITheme.CARD_BG);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Email Label & Input
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setFont(UITheme.FONT_BOLD);
        cardPanel.add(emailLabel, gbc);

        gbc.gridy = 1;
        emailField = new JTextField(20);
        emailField.setFont(UITheme.FONT_REGULAR);
        cardPanel.add(emailField, gbc);

        // Password Label & Input
        gbc.gridy = 2;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(UITheme.FONT_BOLD);
        cardPanel.add(passLabel, gbc);

        gbc.gridy = 3;
        passwordField = new JPasswordField(20);
        passwordField.setFont(UITheme.FONT_REGULAR);
        cardPanel.add(passwordField, gbc);

        // Buttons Panel
        gbc.gridy = 4; gbc.insets = new Insets(18, 5, 5, 5);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);

        JButton loginBtn = UITheme.createPrimaryButton("Login");
        JButton registerBtn = UITheme.createSecondaryButton("Register");

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        cardPanel.add(buttonPanel, gbc);

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // Quick Demo Login Panel for Viva / Testing
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        demoPanel.setOpaque(false);
        demoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR), "Quick Demo Logins (for Viva)"
        ));

        JButton demoAdminBtn = new JButton("Admin");
        JButton demoContribBtn = new JButton("Contributor");
        JButton demoExplorerBtn = new JButton("Explorer");

        demoAdminBtn.setFont(UITheme.FONT_SMALL);
        demoContribBtn.setFont(UITheme.FONT_SMALL);
        demoExplorerBtn.setFont(UITheme.FONT_SMALL);

        demoAdminBtn.addActionListener(e -> {
            emailField.setText("admin@recipe.com");
            passwordField.setText("admin123");
        });

        demoContribBtn.addActionListener(e -> {
            emailField.setText("contributor@recipe.com");
            passwordField.setText("contrib123");
        });

        demoExplorerBtn.addActionListener(e -> {
            emailField.setText("explorer@recipe.com");
            passwordField.setText("explorer123");
        });

        demoPanel.add(demoAdminBtn);
        demoPanel.add(demoContribBtn);
        demoPanel.add(demoExplorerBtn);

        mainPanel.add(demoPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Event Listeners
        loginBtn.addActionListener(e -> performLogin());
        registerBtn.addActionListener(e -> {
            RegisterFrame registerFrame = new RegisterFrame(this);
            registerFrame.setVisible(true);
            this.setVisible(false);
        });
    }

    private void performLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            // Polymorphism: authService returns subclass instance (Admin, Contributor, Explorer)
            User user = authService.login(email, password);

            JOptionPane.showMessageDialog(
                    this,
                    "Login Successful!\nWelcome back, " + user.getName() + " (" + user.getRole() + ")",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Dynamic Polymorphic Call: Each child class launches its own dashboard screen!
            user.showDashboard(this);

        } catch (InvalidLoginException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database Connection Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
