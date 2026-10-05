package com.recipeplatform.gui;

import com.recipeplatform.dao.CollectionDAO;
import com.recipeplatform.dao.UserDAO;
import com.recipeplatform.database.DatabaseConnection;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.model.Recipe;
import com.recipeplatform.model.RecipeExplorer;
import com.recipeplatform.model.Review;
import com.recipeplatform.service.RecipeService;
import com.recipeplatform.service.ReviewService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

// Dashboard Frame for Recipe Explorer role (Polymorphic dashboard implementation)
public class ExplorerDashboard extends JFrame {

    private final RecipeExplorer explorer;
    private final RecipeService recipeService;
    private final ReviewService reviewService;
    private final CollectionDAO collectionDAO;
    private final UserDAO userDAO;

    // Browse Tab UI
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JTable recipeTable;
    private DefaultTableModel recipeTableModel;

    // Saved Collection UI
    private JTable savedTable;
    private DefaultTableModel savedTableModel;

    // My Reviews UI
    private JTable reviewsTable;
    private DefaultTableModel reviewsTableModel;

    // History UI
    private JTable historyTable;
    private DefaultTableModel historyTableModel;

    // Profile UI
    private JTextField profileNameField;
    private JTextField profileEmailField;
    private JPasswordField profilePasswordField;

    public ExplorerDashboard(RecipeExplorer explorer) {
        this.explorer = explorer;
        this.recipeService = new RecipeService();
        this.reviewService = new ReviewService();
        this.collectionDAO = new CollectionDAO();
        this.userDAO = new UserDAO();
        initUI();
        loadApprovedRecipes();
        loadSavedRecipes();
        loadMyReviews();
        loadBrowsingHistory();
        initProfileFields();
    }

    private void initUI() {
        setTitle("Recipe Explorer Dashboard - Online Recipe Sharing Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_COLOR);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("Recipe Explorer Portal");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Welcome, " + explorer.getName() + " (" + explorer.getEmail() + ")");
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

        // Navigation Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        tabbedPane.addTab("Browse Recipes", createBrowsePanel());
        tabbedPane.addTab("Saved Collection (HashSet)", createSavedCollectionPanel());
        tabbedPane.addTab("My Reviews", createMyReviewsPanel());
        tabbedPane.addTab("Browsing History", createHistoryPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    // Tab 1: Browse Recipes
    private JPanel createBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter Bar Top
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        filterPanel.add(searchField);

        filterPanel.add(new JLabel("Category:"));
        categoryFilter = new JComboBox<>(new String[]{
                "All Categories", "Main Course", "Breakfast", "Appetizer", "Dessert", "Beverage", "Snacks"
        });
        filterPanel.add(categoryFilter);

        JButton searchBtn = UITheme.createPrimaryButton("Search");
        JButton resetBtn = UITheme.createSecondaryButton("Reset Filter");

        filterPanel.add(searchBtn);
        filterPanel.add(resetBtn);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Recipe Table
        String[] cols = {"ID", "Title", "Category", "Contributor", "Views", "Average Rating"};
        recipeTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recipeTable = new JTable(recipeTableModel);
        UITheme.styleTable(recipeTable);

        panel.add(new JScrollPane(recipeTable), BorderLayout.CENTER);

        // Action Buttons Bottom
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        JButton viewDetailsBtn = UITheme.createPrimaryButton("View Full Recipe & Reviews");
        btnPanel.add(viewDetailsBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Listeners
        searchBtn.addActionListener(e -> performSearch());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            loadApprovedRecipes();
        });

        viewDetailsBtn.addActionListener(e -> openSelectedRecipeDetails(recipeTable, recipeTableModel));

        return panel;
    }

    // Tab 2: Saved Collection (HashSet requirement)
    private JPanel createSavedCollectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Title", "Category", "Contributor", "Views", "Rating"};
        savedTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        savedTable = new JTable(savedTableModel);
        UITheme.styleTable(savedTable);

        panel.add(new JScrollPane(savedTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        JButton viewBtn = UITheme.createPrimaryButton("View Recipe Details");
        JButton removeBtn = UITheme.createDangerButton("Remove from Collection");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Collection");

        btnPanel.add(viewBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        viewBtn.addActionListener(e -> openSelectedRecipeDetails(savedTable, savedTableModel));

        removeBtn.addActionListener(e -> {
            int row = savedTable.getSelectedRow();
            if (row >= 0) {
                int recipeId = (int) savedTableModel.getValueAt(row, 0);
                try {
                    collectionDAO.removeRecipe(explorer.getId(), recipeId);
                    JOptionPane.showMessageDialog(this, "Recipe removed from your collection!", "Collection", JOptionPane.INFORMATION_MESSAGE);
                    loadSavedRecipes();
                } catch (DatabaseException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a recipe to remove.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> loadSavedRecipes());

        return panel;
    }

    // Tab 3: My Reviews
    private JPanel createMyReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"Review ID", "Recipe", "Rating", "Review Comment", "Submitted Date"};
        reviewsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        reviewsTable = new JTable(reviewsTableModel);
        UITheme.styleTable(reviewsTable);

        panel.add(new JScrollPane(reviewsTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        JButton deleteReviewBtn = UITheme.createDangerButton("Delete My Review");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        btnPanel.add(deleteReviewBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        deleteReviewBtn.addActionListener(e -> {
            int row = reviewsTable.getSelectedRow();
            if (row >= 0) {
                int reviewId = (int) reviewsTableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete your review?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        reviewService.deleteReviewAndRecalculateRating(reviewId, 0);
                        JOptionPane.showMessageDialog(this, "Review deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadMyReviews();
                        loadApprovedRecipes();
                    } catch (DatabaseException ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a review to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> loadMyReviews());

        return panel;
    }

    // Tab 4: Browsing History
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"Recipe ID", "Recipe Title", "Category", "Viewed At"};
        historyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        UITheme.styleTable(historyTable);

        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        JButton viewBtn = UITheme.createPrimaryButton("View Recipe Details");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh History");

        btnPanel.add(viewBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        viewBtn.addActionListener(e -> openSelectedRecipeDetails(historyTable, historyTableModel));
        refreshBtn.addActionListener(e -> loadBrowsingHistory());

        return panel;
    }

    // Tab 5: My Profile
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UITheme.CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0;
        formCard.add(new JLabel("Full Name:"), gbc);
        gbc.gridy = 1;
        profileNameField = new JTextField(20);
        formCard.add(profileNameField, gbc);

        gbc.gridy = 2;
        formCard.add(new JLabel("Email Address:"), gbc);
        gbc.gridy = 3;
        profileEmailField = new JTextField(20);
        formCard.add(profileEmailField, gbc);

        gbc.gridy = 4;
        formCard.add(new JLabel("Password:"), gbc);
        gbc.gridy = 5;
        profilePasswordField = new JPasswordField(20);
        formCard.add(profilePasswordField, gbc);

        gbc.gridy = 6; gbc.insets = new Insets(15, 8, 8, 8);
        JButton saveProfileBtn = UITheme.createPrimaryButton("Update Profile");
        formCard.add(saveProfileBtn, gbc);

        panel.add(formCard);

        saveProfileBtn.addActionListener(e -> updateProfile());

        return panel;
    }

    // Actions & Helpers
    private void openSelectedRecipeDetails(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int recipeId = (int) model.getValueAt(row, 0);
            try {
                Recipe r = recipeService.getRecipeById(recipeId);
                if (r != null) {
                    RecipeDetailsDialog dialog = new RecipeDetailsDialog(this, r, explorer);
                    dialog.setVisible(true);
                    loadApprovedRecipes();
                    loadSavedRecipes();
                    loadMyReviews();
                    loadBrowsingHistory();
                }
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a recipe first.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void performSearch() {
        String query = searchField.getText();
        String category = (String) categoryFilter.getSelectedItem();
        try {
            List<Recipe> filtered = recipeService.searchAndFilterRecipes(query, category);
            populateRecipeTable(filtered);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadApprovedRecipes() {
        try {
            List<Recipe> list = recipeService.getApprovedRecipes();
            populateRecipeTable(list);
        } catch (DatabaseException e) {
            System.err.println("Error loading recipes: " + e.getMessage());
        }
    }

    private void populateRecipeTable(List<Recipe> list) {
        recipeTableModel.setRowCount(0);
        for (Recipe r : list) {
            recipeTableModel.addRow(new Object[]{
                    r.getId(), r.getTitle(), r.getCategory(), r.getContributorName(), r.getViews(), String.format("%.1f ⭐", r.getAvgRating())
            });
        }
    }

    private void loadSavedRecipes() {
        savedTableModel.setRowCount(0);
        try {
            List<Recipe> savedList = collectionDAO.getSavedRecipes(explorer.getId());
            for (Recipe r : savedList) {
                savedTableModel.addRow(new Object[]{
                        r.getId(), r.getTitle(), r.getCategory(), r.getContributorName(), r.getViews(), String.format("%.1f ⭐", r.getAvgRating())
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading saved collection: " + e.getMessage());
        }
    }

    private void loadMyReviews() {
        reviewsTableModel.setRowCount(0);
        try {
            List<Review> list = reviewService.getReviewsByUser(explorer.getId());
            for (Review r : list) {
                reviewsTableModel.addRow(new Object[]{
                        r.getId(), r.getRecipeTitle(), r.getRating() + " ⭐", r.getReviewText(), r.getCreatedAt()
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading reviews: " + e.getMessage());
        }
    }

    private void loadBrowsingHistory() {
        historyTableModel.setRowCount(0);
        String sql = "SELECT rv.viewed_at, r.id, r.title, r.category " +
                     "FROM recipe_views rv " +
                     "JOIN recipes r ON rv.recipe_id = r.id " +
                     "WHERE rv.user_id = ? ORDER BY rv.id DESC LIMIT 20";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, explorer.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historyTableModel.addRow(new Object[]{
                            rs.getInt("id"), rs.getString("title"), rs.getString("category"), rs.getString("viewed_at")
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading history: " + e.getMessage());
        }
    }

    private void initProfileFields() {
        profileNameField.setText(explorer.getName());
        profileEmailField.setText(explorer.getEmail());
        profilePasswordField.setText(explorer.getPassword());
    }

    private void updateProfile() {
        String name = profileNameField.getText().trim();
        String email = profileEmailField.getText().trim();
        String pass = new String(profilePasswordField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All profile fields are required.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        explorer.setName(name);
        explorer.setEmail(email);
        explorer.setPassword(pass);

        try {
            userDAO.updateUser(explorer);
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
