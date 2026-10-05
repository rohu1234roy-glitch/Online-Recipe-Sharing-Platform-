package com.recipeplatform.gui;

import com.recipeplatform.dao.RecipeDAO;
import com.recipeplatform.dao.ReviewDAO;
import com.recipeplatform.dao.UserDAO;
import com.recipeplatform.database.DatabaseConnection;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.model.Admin;
import com.recipeplatform.model.Recipe;
import com.recipeplatform.model.Review;
import com.recipeplatform.model.User;
import com.recipeplatform.service.RecipeService;
import com.recipeplatform.service.ReviewService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

// Dashboard Frame for Admin user role (Polymorphic dashboard implementation)
public class AdminDashboard extends JFrame {

    private final Admin admin;
    private final UserDAO userDAO;
    private final RecipeDAO recipeDAO;
    private final RecipeService recipeService;
    private final ReviewService reviewService;

    // Tables & Models
    private JTable userTable;
    private DefaultTableModel userTableModel;

    private JTable recipeTable;
    private DefaultTableModel recipeTableModel;

    private JTable reviewTable;
    private DefaultTableModel reviewTableModel;

    private JTable settingsTable;
    private DefaultTableModel settingsTableModel;

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        this.userDAO = new UserDAO();
        this.recipeDAO = new RecipeDAO();
        this.recipeService = new RecipeService();
        this.reviewService = new ReviewService();
        initUI();
        loadAllData();
    }

    private void initUI() {
        setTitle("Admin Dashboard - Online Recipe Sharing Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_COLOR);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("Admin Control Panel");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Welcome, " + admin.getName() + " (" + admin.getEmail() + ")");
        welcomeLabel.setFont(UITheme.FONT_REGULAR);
        welcomeLabel.setForeground(new Color(220, 245, 220));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(titleLabel);
        titleBox.add(welcomeLabel);

        JButton logoutBtn = UITheme.createSecondaryButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane for Navigation
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        tabbedPane.addTab("Manage Users", createUsersPanel());
        tabbedPane.addTab("Manage Recipes", createRecipesPanel());
        tabbedPane.addTab("Content Moderation", createModerationPanel());
        tabbedPane.addTab("System Settings", createSettingsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    // 1. Users Panel
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Name", "Email", "Role"};
        userTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        userTable = new JTable(userTableModel);
        UITheme.styleTable(userTable);

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Action bar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = UITheme.createPrimaryButton("Add User");
        JButton editBtn = UITheme.createSecondaryButton("Edit User");
        JButton deleteBtn = UITheme.createDangerButton("Delete User");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Listeners
        addBtn.addActionListener(e -> {
            UserFormDialog dialog = new UserFormDialog(this, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadUsers();
        });

        editBtn.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a user to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int userId = (int) userTableModel.getValueAt(selectedRow, 0);
            try {
                User userToEdit = userDAO.getUserById(userId);
                if (userToEdit != null) {
                    UserFormDialog dialog = new UserFormDialog(this, userToEdit);
                    dialog.setVisible(true);
                    if (dialog.isSaved()) loadUsers();
                }
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int userId = (int) userTableModel.getValueAt(selectedRow, 0);
            if (userId == admin.getId()) {
                JOptionPane.showMessageDialog(this, "You cannot delete your own admin account!", "Action Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?\nAll associated data will be removed using Database Transaction.", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // Uses Transaction Management (setAutoCommit(false), commit, rollback)
                    userDAO.deleteUserWithTransaction(userId);
                    JOptionPane.showMessageDialog(this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                } catch (DatabaseException ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting user: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        refreshBtn.addActionListener(e -> loadUsers());

        return panel;
    }

    // 2. Recipes Panel
    private JPanel createRecipesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Title", "Category", "Contributor", "Status", "Rating"};
        recipeTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        recipeTable = new JTable(recipeTableModel);
        UITheme.styleTable(recipeTable);

        panel.add(new JScrollPane(recipeTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        JButton approveBtn = UITheme.createPrimaryButton("Approve Recipe");
        JButton rejectBtn = UITheme.createSecondaryButton("Reject Recipe");
        JButton deleteBtn = UITheme.createDangerButton("Delete Recipe");
        JButton viewBtn = UITheme.createSecondaryButton("View Details");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(viewBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> {
            int selectedRow = recipeTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) recipeTableModel.getValueAt(selectedRow, 0);
                try {
                    recipeService.approveRecipe(id);
                    JOptionPane.showMessageDialog(this, "Recipe approved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadRecipes();
                } catch (DatabaseException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a recipe first.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        rejectBtn.addActionListener(e -> {
            int selectedRow = recipeTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) recipeTableModel.getValueAt(selectedRow, 0);
                try {
                    recipeService.rejectRecipe(id);
                    JOptionPane.showMessageDialog(this, "Recipe rejected!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadRecipes();
                } catch (DatabaseException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a recipe first.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = recipeTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) recipeTableModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this recipe and associated reviews via Transaction?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        recipeService.deleteRecipe(id);
                        JOptionPane.showMessageDialog(this, "Recipe deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadRecipes();
                    } catch (DatabaseException ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a recipe first.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        viewBtn.addActionListener(e -> {
            int selectedRow = recipeTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) recipeTableModel.getValueAt(selectedRow, 0);
                try {
                    Recipe r = recipeService.getRecipeById(id);
                    if (r != null) {
                        RecipeDetailsDialog dialog = new RecipeDetailsDialog(this, r, admin);
                        dialog.setVisible(true);
                    }
                } catch (DatabaseException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a recipe first.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> loadRecipes());

        return panel;
    }

    // 3. Moderation Panel
    private JPanel createModerationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Recipe", "User", "Rating", "Review Comment", "Date"};
        reviewTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        reviewTable = new JTable(reviewTableModel);
        UITheme.styleTable(reviewTable);

        panel.add(new JScrollPane(reviewTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        JButton deleteReviewBtn = UITheme.createDangerButton("Delete Review");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        btnPanel.add(deleteReviewBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        deleteReviewBtn.addActionListener(e -> {
            int selectedRow = reviewTable.getSelectedRow();
            if (selectedRow >= 0) {
                int reviewId = (int) reviewTableModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this review?", "Confirm Moderation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        reviewService.deleteReviewAndRecalculateRating(reviewId, 0);
                        JOptionPane.showMessageDialog(this, "Review deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadReviews();
                    } catch (DatabaseException ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a review to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> loadReviews());

        return panel;
    }

    // 4. System Settings Panel
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Setting Name", "Value"};
        settingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        settingsTable = new JTable(settingsTableModel);
        UITheme.styleTable(settingsTable);

        panel.add(new JScrollPane(settingsTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        JButton editSettingBtn = UITheme.createPrimaryButton("Update Setting Value");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        btnPanel.add(editSettingBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        editSettingBtn.addActionListener(e -> {
            int selectedRow = settingsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) settingsTableModel.getValueAt(selectedRow, 0);
                String name = (String) settingsTableModel.getValueAt(selectedRow, 1);
                String currentVal = (String) settingsTableModel.getValueAt(selectedRow, 2);

                String newVal = JOptionPane.showInputDialog(this, "Enter new value for " + name + ":", currentVal);
                if (newVal != null && !newVal.trim().isEmpty()) {
                    updateSystemSetting(id, newVal.trim());
                    loadSettings();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a setting to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> loadSettings());

        return panel;
    }

    // Data Loaders
    private void loadAllData() {
        loadUsers();
        loadRecipes();
        loadReviews();
        loadSettings();
    }

    private void loadUsers() {
        userTableModel.setRowCount(0);
        try {
            List<User> list = userDAO.getAllUsers();
            for (User u : list) {
                userTableModel.addRow(new Object[]{u.getId(), u.getName(), u.getEmail(), u.getRole()});
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void loadRecipes() {
        recipeTableModel.setRowCount(0);
        try {
            List<Recipe> list = recipeDAO.getAllRecipes();
            for (Recipe r : list) {
                recipeTableModel.addRow(new Object[]{
                        r.getId(), r.getTitle(), r.getCategory(), r.getContributorName(), r.getStatus(), String.format("%.1f ⭐", r.getAvgRating())
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading recipes: " + e.getMessage());
        }
    }

    private void loadReviews() {
        reviewTableModel.setRowCount(0);
        try {
            List<Review> list = reviewService.getAllReviews();
            for (Review r : list) {
                reviewTableModel.addRow(new Object[]{
                        r.getId(), r.getRecipeTitle(), r.getUserName(), r.getRating() + " ⭐", r.getReviewText(), r.getCreatedAt()
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading reviews: " + e.getMessage());
        }
    }

    private void loadSettings() {
        settingsTableModel.setRowCount(0);
        String sql = "SELECT * FROM system_settings ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                settingsTableModel.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("setting_name"), rs.getString("setting_value")
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading settings: " + e.getMessage());
        }
    }

    private void updateSystemSetting(int id, String value) {
        String sql = "UPDATE system_settings SET setting_value = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value);
            ps.setInt(2, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Setting updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating setting: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
