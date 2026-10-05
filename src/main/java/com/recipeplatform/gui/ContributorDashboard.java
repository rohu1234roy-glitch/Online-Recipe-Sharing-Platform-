package com.recipeplatform.gui;

import com.recipeplatform.dao.MessageDAO;
import com.recipeplatform.dao.UserDAO;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.model.Message;
import com.recipeplatform.model.Recipe;
import com.recipeplatform.model.RecipeContributor;
import com.recipeplatform.model.User;
import com.recipeplatform.service.RecipeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Dashboard Frame for Recipe Contributor role (Demonstrates Multithreading & Asynchronous Data Loading)
public class ContributorDashboard extends JFrame {

    private final RecipeContributor contributor;
    private final RecipeService recipeService;
    private final MessageDAO messageDAO;
    private final UserDAO userDAO;

    // My Recipes Table
    private JTable recipeTable;
    private DefaultTableModel recipeTableModel;

    // Stats UI Labels (Updated by Multithreading Background Worker)
    private JLabel totalRecipesLabel;
    private JLabel approvedRecipesLabel;
    private JLabel pendingRecipesLabel;
    private JLabel avgRatingLabel;
    private JLabel totalViewsLabel;
    private JLabel statsStatusLabel;

    // Messages Table & Fields
    private JTable messagesTable;
    private DefaultTableModel messagesTableModel;
    private JComboBox<UserItem> receiverComboBox;
    private JTextArea messageTextArea;

    // Profile Fields
    private JTextField profileNameField;
    private JTextField profileEmailField;
    private JPasswordField profilePasswordField;

    public ContributorDashboard(RecipeContributor contributor) {
        this.contributor = contributor;
        this.recipeService = new RecipeService();
        this.messageDAO = new MessageDAO();
        this.userDAO = new UserDAO();
        initUI();
        loadRecipes();
        loadStatsAsync();
        loadMessages();
        loadRecipientDropdown();
        initProfileFields();
    }

    private void initUI() {
        setTitle("Contributor Dashboard - Recipe Sharing Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_COLOR);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("Contributor Dashboard");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Welcome, " + contributor.getName() + " (" + contributor.getEmail() + ")");
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

        tabbedPane.addTab("My Recipes", createMyRecipesPanel());
        tabbedPane.addTab("Recipe Statistics (Multithreaded)", createStatsPanel());
        tabbedPane.addTab("Messages", createMessagesPanel());
        tabbedPane.addTab("My Profile", createProfilePanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    // Tab 1: My Recipes
    private JPanel createMyRecipesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = {"ID", "Title", "Category", "Status", "Views", "Average Rating"};
        recipeTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        recipeTable = new JTable(recipeTableModel);
        UITheme.styleTable(recipeTable);

        panel.add(new JScrollPane(recipeTable), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = UITheme.createPrimaryButton("Add New Recipe");
        JButton editBtn = UITheme.createSecondaryButton("Edit Recipe");
        JButton deleteBtn = UITheme.createDangerButton("Delete Recipe");
        JButton viewBtn = UITheme.createSecondaryButton("View Details");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(viewBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            RecipeFormDialog dialog = new RecipeFormDialog(this, null, contributor.getId(), contributor.getRole());
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadRecipes();
                loadStatsAsync();
            }
        });

        editBtn.addActionListener(e -> {
            int row = recipeTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) recipeTableModel.getValueAt(row, 0);
                try {
                    Recipe r = recipeService.getRecipeById(id);
                    if (r != null) {
                        RecipeFormDialog dialog = new RecipeFormDialog(this, r, contributor.getId(), contributor.getRole());
                        dialog.setVisible(true);
                        if (dialog.isSaved()) {
                            loadRecipes();
                            loadStatsAsync();
                        }
                    }
                } catch (DatabaseException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a recipe to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = recipeTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) recipeTableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this recipe?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        recipeService.deleteRecipe(id);
                        loadRecipes();
                        loadStatsAsync();
                    } catch (DatabaseException ex) {
                        JOptionPane.showMessageDialog(this, "Error deleting recipe: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a recipe to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        viewBtn.addActionListener(e -> {
            int row = recipeTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) recipeTableModel.getValueAt(row, 0);
                try {
                    Recipe r = recipeService.getRecipeById(id);
                    if (r != null) {
                        RecipeDetailsDialog dialog = new RecipeDetailsDialog(this, r, contributor);
                        dialog.setVisible(true);
                        loadRecipes();
                    }
                } catch (DatabaseException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a recipe.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> {
            loadRecipes();
            loadStatsAsync();
        });

        return panel;
    }

    // Tab 2: Statistics (Demonstrates Multithreading requirement)
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel("Live Recipe Performance Statistics (Background Thread)");
        titleLabel.setFont(UITheme.FONT_HEADER);

        statsStatusLabel = new JLabel("Loading statistics in background thread...");
        statsStatusLabel.setFont(UITheme.FONT_SMALL);
        statsStatusLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.setOpaque(false);
        topBox.add(titleLabel);
        topBox.add(Box.createRigidArea(new Dimension(0, 5)));
        topBox.add(statsStatusLabel);

        panel.add(topBox, BorderLayout.NORTH);

        // Stats Cards Grid
        JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 15, 15));
        cardsGrid.setOpaque(false);

        totalRecipesLabel = createStatValueLabel("0");
        approvedRecipesLabel = createStatValueLabel("0");
        pendingRecipesLabel = createStatValueLabel("0");
        avgRatingLabel = createStatValueLabel("0.0 ⭐");
        totalViewsLabel = createStatValueLabel("0 👁️");

        cardsGrid.add(createCard("Total Recipes", totalRecipesLabel, new Color(227, 242, 253)));
        cardsGrid.add(createCard("Approved Recipes", approvedRecipesLabel, new Color(232, 245, 233)));
        cardsGrid.add(createCard("Pending Approval", pendingRecipesLabel, new Color(255, 243, 224)));
        cardsGrid.add(createCard("Average Rating", avgRatingLabel, new Color(243, 229, 245)));
        cardsGrid.add(createCard("Total Views", totalViewsLabel, new Color(239, 235, 233)));

        panel.add(cardsGrid, BorderLayout.CENTER);

        JButton refreshStatsBtn = UITheme.createPrimaryButton("Refresh Statistics");
        refreshStatsBtn.addActionListener(e -> loadStatsAsync());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(refreshStatsBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createStatValueLabel(String initialText) {
        JLabel label = new JLabel(initialText, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(UITheme.PRIMARY_COLOR);
        return label;
    }

    private JPanel createCard(String title, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_BOLD);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // Tab 3: Messages
    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Inbox Table
        String[] cols = {"ID", "From", "To", "Message", "Sent Date"};
        messagesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        messagesTable = new JTable(messagesTableModel);
        UITheme.styleTable(messagesTable);

        JPanel inboxPanel = new JPanel(new BorderLayout());
        inboxPanel.setOpaque(false);
        inboxPanel.setBorder(BorderFactory.createTitledBorder("Messages Inbox"));
        inboxPanel.add(new JScrollPane(messagesTable), BorderLayout.CENTER);

        panel.add(inboxPanel, BorderLayout.CENTER);

        // Send Message Form
        JPanel sendPanel = new JPanel(new GridBagLayout());
        sendPanel.setOpaque(false);
        sendPanel.setBorder(BorderFactory.createTitledBorder("Send Message"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        sendPanel.add(new JLabel("Recipient:"), gbc);

        gbc.gridx = 1;
        receiverComboBox = new JComboBox<>();
        sendPanel.add(receiverComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        sendPanel.add(new JLabel("Message:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        messageTextArea = new JTextArea(2, 25);
        messageTextArea.setLineWrap(true);
        sendPanel.add(new JScrollPane(messageTextArea), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1;
        JButton sendBtn = UITheme.createPrimaryButton("Send Message");
        sendPanel.add(sendBtn, gbc);

        panel.add(sendPanel, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendMessage());

        return panel;
    }

    // Tab 4: Profile
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

    // Data Loaders
    private void loadRecipes() {
        recipeTableModel.setRowCount(0);
        try {
            List<Recipe> list = recipeService.getContributorRecipes(contributor.getId());
            for (Recipe r : list) {
                recipeTableModel.addRow(new Object[]{
                        r.getId(), r.getTitle(), r.getCategory(), r.getStatus(), r.getViews(), String.format("%.1f ⭐", r.getAvgRating())
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading contributor recipes: " + e.getMessage());
        }
    }

    // Demonstrates Multithreading (Background thread statistics calculation)
    private void loadStatsAsync() {
        statsStatusLabel.setText("Computing statistics in background thread...");
        recipeService.loadContributorStatsAsync(contributor.getId(), new RecipeService.StatsCallback() {
            @Override
            public void onSuccess(RecipeService.ContributorStats stats) {
                totalRecipesLabel.setText(String.valueOf(stats.totalRecipes));
                approvedRecipesLabel.setText(String.valueOf(stats.approvedCount));
                pendingRecipesLabel.setText(String.valueOf(stats.pendingCount));
                avgRatingLabel.setText(String.format("%.1f ⭐", stats.averageRating));
                totalViewsLabel.setText(String.format("%d 👁️", stats.totalViews));
                statsStatusLabel.setText("Updated using background Multithreading.");
            }

            @Override
            public void onError(String errorMessage) {
                statsStatusLabel.setText("Failed to load stats: " + errorMessage);
            }
        });
    }

    private void loadMessages() {
        messagesTableModel.setRowCount(0);
        try {
            List<Message> list = messageDAO.getMessagesForUser(contributor.getId());
            for (Message m : list) {
                messagesTableModel.addRow(new Object[]{
                        m.getId(), m.getSenderName(), m.getReceiverName(), m.getMessageText(), m.getSentAt()
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading messages: " + e.getMessage());
        }
    }

    private static class UserItem {
        int id;
        String name;
        String role;
        UserItem(int id, String name, String role) {
            this.id = id; this.name = name; this.role = role;
        }
        @Override
        public String toString() { return name + " (" + role + ")"; }
    }

    private void loadRecipientDropdown() {
        receiverComboBox.removeAllItems();
        try {
            List<User> allUsers = userDAO.getAllUsers();
            for (User u : allUsers) {
                if (u.getId() != contributor.getId()) {
                    receiverComboBox.addItem(new UserItem(u.getId(), u.getName(), u.getRole()));
                }
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading recipients: " + e.getMessage());
        }
    }

    private void sendMessage() {
        UserItem recipient = (UserItem) receiverComboBox.getSelectedItem();
        String text = messageTextArea.getText().trim();
        if (recipient == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select recipient and enter message.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Message msg = new Message();
        msg.setSenderId(contributor.getId());
        msg.setReceiverId(recipient.id);
        msg.setMessageText(text);

        try {
            messageDAO.sendMessage(msg);
            JOptionPane.showMessageDialog(this, "Message sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            messageTextArea.setText("");
            loadMessages();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Error sending message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initProfileFields() {
        profileNameField.setText(contributor.getName());
        profileEmailField.setText(contributor.getEmail());
        profilePasswordField.setText(contributor.getPassword());
    }

    private void updateProfile() {
        String name = profileNameField.getText().trim();
        String email = profileEmailField.getText().trim();
        String pass = new String(profilePasswordField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All profile fields are required.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        contributor.setName(name);
        contributor.setEmail(email);
        contributor.setPassword(pass);

        try {
            userDAO.updateUser(contributor);
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
