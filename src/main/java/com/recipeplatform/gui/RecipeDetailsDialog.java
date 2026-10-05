package com.recipeplatform.gui;

import com.recipeplatform.dao.CollectionDAO;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.DuplicateReviewException;
import com.recipeplatform.model.Recipe;
import com.recipeplatform.model.Review;
import com.recipeplatform.model.User;
import com.recipeplatform.service.RecipeService;
import com.recipeplatform.service.ReviewService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Set;

// Dialog to view full recipe details, write reviews, and manage personal saved collection
public class RecipeDetailsDialog extends JDialog {

    private final Recipe recipe;
    private final User currentUser;
    private final RecipeService recipeService;
    private final ReviewService reviewService;
    private final CollectionDAO collectionDAO;

    private JLabel ratingLabel;
    private JLabel viewsLabel;
    private JButton saveCollectionBtn;
    private JTable reviewsTable;
    private DefaultTableModel reviewsTableModel;

    private JComboBox<Integer> ratingCombo;
    private JTextArea reviewCommentArea;

    private boolean isSavedInCollection = false;

    public RecipeDetailsDialog(Frame parent, Recipe recipe, User currentUser) {
        super(parent, "Recipe Details - " + recipe.getTitle(), true);
        this.recipe = recipe;
        this.currentUser = currentUser;
        this.recipeService = new RecipeService();
        this.reviewService = new ReviewService();
        this.collectionDAO = new CollectionDAO();

        // Increment recipe view count on open
        logRecipeView();
        initUI();
        loadCollectionState();
        loadReviews();
    }

    private void logRecipeView() {
        if (currentUser != null && recipe != null) {
            try {
                recipeService.incrementViews(recipe.getId(), currentUser.getId());
                recipe.setViews(recipe.getViews() + 1);
            } catch (DatabaseException e) {
                System.err.println("Failed to log view count: " + e.getMessage());
            }
        }
    }

    private void initUI() {
        setSize(700, 650);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(recipe.getTitle());
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_COLOR);

        JLabel metaLabel = new JLabel(String.format("Category: %s | Contributor: %s",
                recipe.getCategory(), recipe.getContributorName()));
        metaLabel.setFont(UITheme.FONT_REGULAR);
        metaLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(titleLabel);
        titleBox.add(metaLabel);

        headerPanel.add(titleBox, BorderLayout.WEST);

        // Stats & Save Button Top Right
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statsPanel.setOpaque(false);

        ratingLabel = new JLabel(String.format("Rating: %.1f ⭐ (%d reviews)", recipe.getAvgRating(), recipe.getTotalRatings()));
        ratingLabel.setFont(UITheme.FONT_BOLD);

        viewsLabel = new JLabel(String.format("Views: %d 👁️", recipe.getViews()));
        viewsLabel.setFont(UITheme.FONT_REGULAR);

        saveCollectionBtn = UITheme.createSecondaryButton("Save Recipe");

        statsPanel.add(ratingLabel);
        statsPanel.add(viewsLabel);
        if ("Explorer".equalsIgnoreCase(currentUser.getRole())) {
            statsPanel.add(saveCollectionBtn);
        }

        headerPanel.add(statsPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Split Pane / Tabbed View (Details vs Reviews)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        // Tab 1: Ingredients & Instructions
        JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        detailsPanel.setBackground(UITheme.CARD_BG);
        detailsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Ingredients Section
        JPanel ingredientsBox = new JPanel(new BorderLayout());
        ingredientsBox.setOpaque(false);
        JLabel ingTitle = new JLabel("Ingredients:");
        ingTitle.setFont(UITheme.FONT_HEADER);
        JTextArea ingArea = new JTextArea(recipe.getIngredients());
        ingArea.setFont(UITheme.FONT_REGULAR);
        ingArea.setEditable(false);
        ingArea.setLineWrap(true);
        ingArea.setWrapStyleWord(true);
        ingredientsBox.add(ingTitle, BorderLayout.NORTH);
        ingredientsBox.add(new JScrollPane(ingArea), BorderLayout.CENTER);

        // Instructions Section
        JPanel instructionsBox = new JPanel(new BorderLayout());
        instructionsBox.setOpaque(false);
        JLabel instTitle = new JLabel("Preparation Instructions:");
        instTitle.setFont(UITheme.FONT_HEADER);
        JTextArea instArea = new JTextArea(recipe.getInstructions());
        instArea.setFont(UITheme.FONT_REGULAR);
        instArea.setEditable(false);
        instArea.setLineWrap(true);
        instArea.setWrapStyleWord(true);
        instructionsBox.add(instTitle, BorderLayout.NORTH);
        instructionsBox.add(new JScrollPane(instArea), BorderLayout.CENTER);

        detailsPanel.add(ingredientsBox);
        detailsPanel.add(instructionsBox);

        tabbedPane.addTab("Recipe Details", detailsPanel);

        // Tab 2: Reviews & Add Review
        JPanel reviewTabPanel = new JPanel(new BorderLayout(10, 10));
        reviewTabPanel.setBackground(UITheme.CARD_BG);
        reviewTabPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Reviews JTable
        String[] cols = {"User", "Rating", "Review", "Date"};
        reviewsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reviewsTable = new JTable(reviewsTableModel);
        UITheme.styleTable(reviewsTable);
        reviewTabPanel.add(new JScrollPane(reviewsTable), BorderLayout.CENTER);

        // Write Review Panel (Only for Explorers)
        if ("Explorer".equalsIgnoreCase(currentUser.getRole())) {
            JPanel addReviewPanel = new JPanel(new GridBagLayout());
            addReviewPanel.setOpaque(false);
            addReviewPanel.setBorder(BorderFactory.createTitledBorder("Write a Review"));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0; gbc.gridy = 0;
            addReviewPanel.add(new JLabel("Rating (1-5):"), gbc);

            gbc.gridx = 1;
            ratingCombo = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
            addReviewPanel.add(ratingCombo, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            addReviewPanel.add(new JLabel("Comment:"), gbc);

            gbc.gridx = 1; gbc.gridwidth = 2;
            reviewCommentArea = new JTextArea(2, 25);
            reviewCommentArea.setLineWrap(true);
            addReviewPanel.add(new JScrollPane(reviewCommentArea), gbc);

            gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1;
            JButton submitReviewBtn = UITheme.createPrimaryButton("Submit Review");
            addReviewPanel.add(submitReviewBtn, gbc);

            submitReviewBtn.addActionListener(e -> submitReview());
            reviewTabPanel.add(addReviewPanel, BorderLayout.SOUTH);
        }

        tabbedPane.addTab("Reviews & Ratings", reviewTabPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Close Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        saveCollectionBtn.addActionListener(e -> toggleSaveCollection());
    }

    private void loadCollectionState() {
        if (!"Explorer".equalsIgnoreCase(currentUser.getRole())) return;
        try {
            // Collections HashSet requirement: check saved IDs using HashSet
            Set<Integer> savedIds = collectionDAO.getSavedRecipeIds(currentUser.getId());
            isSavedInCollection = savedIds.contains(recipe.getId());
            updateSaveButtonUI();
        } catch (DatabaseException e) {
            System.err.println("Error fetching saved state: " + e.getMessage());
        }
    }

    private void updateSaveButtonUI() {
        if (isSavedInCollection) {
            saveCollectionBtn.setText("Saved ❤️");
            saveCollectionBtn.setBackground(new Color(232, 245, 233));
        } else {
            saveCollectionBtn.setText("Save Recipe");
            saveCollectionBtn.setBackground(new Color(233, 236, 239));
        }
    }

    private void toggleSaveCollection() {
        try {
            if (isSavedInCollection) {
                collectionDAO.removeRecipe(currentUser.getId(), recipe.getId());
                isSavedInCollection = false;
                JOptionPane.showMessageDialog(this, "Removed from saved recipes.", "Collection", JOptionPane.INFORMATION_MESSAGE);
            } else {
                collectionDAO.saveRecipe(currentUser.getId(), recipe.getId());
                isSavedInCollection = true;
                JOptionPane.showMessageDialog(this, "Recipe saved to your collection!", "Collection", JOptionPane.INFORMATION_MESSAGE);
            }
            updateSaveButtonUI();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReviews() {
        reviewsTableModel.setRowCount(0);
        try {
            List<Review> reviewList = reviewService.getReviewsForRecipe(recipe.getId());
            for (Review r : reviewList) {
                reviewsTableModel.addRow(new Object[]{
                        r.getUserName(),
                        r.getRating() + " ⭐",
                        r.getReviewText(),
                        r.getCreatedAt()
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Failed to load reviews: " + e.getMessage());
        }
    }

    private void submitReview() {
        int rating = (Integer) ratingCombo.getSelectedItem();
        String comment = reviewCommentArea.getText().trim();

        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a comment for your review.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Review review = new Review();
        review.setRecipeId(recipe.getId());
        review.setUserId(currentUser.getId());
        review.setRating(rating);
        review.setReviewText(comment);

        try {
            // Thread-safe rating calculation & review submission
            boolean success = reviewService.addReviewAndRecalculateRating(review);
            if (success) {
                JOptionPane.showMessageDialog(this, "Review submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                reviewCommentArea.setText("");
                loadReviews();

                // Refresh updated rating metadata
                Recipe updated = recipeService.getRecipeById(recipe.getId());
                if (updated != null) {
                    ratingLabel.setText(String.format("Rating: %.1f ⭐ (%d reviews)", updated.getAvgRating(), updated.getTotalRatings()));
                }
            }
        } catch (DuplicateReviewException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Duplicate Review", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
