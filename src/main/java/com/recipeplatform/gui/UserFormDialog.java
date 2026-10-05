package com.recipeplatform.gui;

import com.recipeplatform.dao.UserDAO;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.model.Admin;
import com.recipeplatform.model.RecipeContributor;
import com.recipeplatform.model.RecipeExplorer;
import com.recipeplatform.model.User;
import com.recipeplatform.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Dialog for Admin to Add or Edit Users
public class UserFormDialog extends JDialog {

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    private User targetUser; // If editing
    private boolean saved = false;
    private final UserDAO userDAO;

    public UserFormDialog(Frame parent, User userToEdit) {
        super(parent, userToEdit == null ? "Add New User" : "Edit User", true);
        this.targetUser = userToEdit;
        this.userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        setSize(420, 380);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridy = 1;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        // Email
        gbc.gridy = 2;
        formPanel.add(new JLabel("Email Address:"), gbc);
        gbc.gridy = 3;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridy = 4;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridy = 5;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);

        // Role
        gbc.gridy = 6;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridy = 7;
        roleComboBox = new JComboBox<>(new String[]{"Admin", "Contributor", "Explorer"});
        formPanel.add(roleComboBox, gbc);

        // Populate fields if editing
        if (targetUser != null) {
            nameField.setText(targetUser.getName());
            emailField.setText(targetUser.getEmail());
            passwordField.setText(targetUser.getPassword());
            roleComboBox.setSelectedItem(targetUser.getRole());
            roleComboBox.setEnabled(false); // Role changes locked when editing existing user
        }

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton saveBtn = UITheme.createPrimaryButton("Save");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);

        saveBtn.addActionListener(e -> saveUser());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void saveUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (ValidationUtil.isEmpty(name) || ValidationUtil.isEmpty(email) || ValidationUtil.isEmpty(password)) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (targetUser == null) {
                // Creating new user
                User newUser;
                if ("Admin".equalsIgnoreCase(role)) {
                    newUser = new Admin(name, email, password);
                } else if ("Contributor".equalsIgnoreCase(role)) {
                    newUser = new RecipeContributor(name, email, password);
                } else {
                    newUser = new RecipeExplorer(name, email, password);
                }
                userDAO.addUser(newUser);
            } else {
                // Updating existing user
                targetUser.setName(name);
                targetUser.setEmail(email);
                targetUser.setPassword(password);
                userDAO.updateUser(targetUser);
            }
            saved = true;
            dispose();
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
