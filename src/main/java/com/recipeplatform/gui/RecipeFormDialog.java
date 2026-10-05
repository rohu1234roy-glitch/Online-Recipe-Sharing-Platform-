package com.recipeplatform.gui;

import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.RecipeValidationException;
import com.recipeplatform.model.Recipe;
import com.recipeplatform.service.RecipeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Dialog Form for Adding or Editing Recipes
public class RecipeFormDialog extends JDialog {

    private JTextField titleField;
    private JTextField descriptionField;
    private JComboBox<String> categoryComboBox;
    private JTextArea ingredientsArea;
    private JTextArea instructionsArea;
    private JTextField imagePathField;

    private Recipe targetRecipe;
    private final int currentUserId;
    private final String userRole;
    private boolean saved = false;
    private final RecipeService recipeService;

    public RecipeFormDialog(Frame parent, Recipe recipeToEdit, int userId, String userRole) {
        super(parent, recipeToEdit == null ? "Add New Recipe" : "Edit Recipe", true);
        this.targetRecipe = recipeToEdit;
        this.currentUserId = userId;
        this.userRole = userRole;
        this.recipeService = new RecipeService();
        initUI();
    }

    private void initUI() {
        setSize(550, 620);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Recipe Title:"), gbc);
        gbc.gridy = 1;
        titleField = new JTextField(30);
        formPanel.add(titleField, gbc);

        // Category
        gbc.gridy = 2;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridy = 3;
        categoryComboBox = new JComboBox<>(new String[]{
                "Main Course", "Breakfast", "Appetizer", "Dessert", "Beverage", "Snacks"
        });
        formPanel.add(categoryComboBox, gbc);

        // Short Description
        gbc.gridy = 4;
        formPanel.add(new JLabel("Short Description:"), gbc);
        gbc.gridy = 5;
        descriptionField = new JTextField(30);
        formPanel.add(descriptionField, gbc);

        // Ingredients
        gbc.gridy = 6;
        formPanel.add(new JLabel("Ingredients (comma separated):"), gbc);
        gbc.gridy = 7;
        ingredientsArea = new JTextArea(3, 30);
        ingredientsArea.setLineWrap(true);
        ingredientsArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(ingredientsArea), gbc);

        // Instructions
        gbc.gridy = 8;
        formPanel.add(new JLabel("Preparation Instructions:"), gbc);
        gbc.gridy = 9;
        instructionsArea = new JTextArea(4, 30);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(instructionsArea), gbc);

        // Image Path (Optional)
        gbc.gridy = 10;
        formPanel.add(new JLabel("Image Path (Optional):"), gbc);
        gbc.gridy = 11;
        imagePathField = new JTextField(30);
        formPanel.add(imagePathField, gbc);

        // Populate if editing
        if (targetRecipe != null) {
            titleField.setText(targetRecipe.getTitle());
            categoryComboBox.setSelectedItem(targetRecipe.getCategory());
            descriptionField.setText(targetRecipe.getDescription());
            ingredientsArea.setText(targetRecipe.getIngredients());
            instructionsArea.setText(targetRecipe.getInstructions());
            imagePathField.setText(targetRecipe.getImagePath());
        }

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton saveBtn = UITheme.createPrimaryButton("Save Recipe");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);

        saveBtn.addActionListener(e -> saveRecipe());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void saveRecipe() {
        String title = titleField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();
        String description = descriptionField.getText().trim();
        String ingredients = ingredientsArea.getText().trim();
        String instructions = instructionsArea.getText().trim();
        String imagePath = imagePathField.getText().trim();

        try {
            if (targetRecipe == null) {
                // New recipe submission (starts as PENDING for contributors)
                Recipe recipe = new Recipe();
                recipe.setTitle(title);
                recipe.setCategory(category);
                recipe.setDescription(description);
                recipe.setIngredients(ingredients);
                recipe.setInstructions(instructions);
                recipe.setImagePath(imagePath);
                recipe.setContributorId(currentUserId);
                // Admin can auto-approve, Contributor recipes stay PENDING
                recipe.setStatus("Admin".equalsIgnoreCase(userRole) ? "APPROVED" : "PENDING");

                recipeService.addRecipe(recipe);
                JOptionPane.showMessageDialog(this, "Recipe submitted successfully!\n" +
                        ("Contributor".equalsIgnoreCase(userRole) ? "Status is PENDING admin approval." : "Status is APPROVED."),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Edit existing recipe
                targetRecipe.setTitle(title);
                targetRecipe.setCategory(category);
                targetRecipe.setDescription(description);
                targetRecipe.setIngredients(ingredients);
                targetRecipe.setInstructions(instructions);
                targetRecipe.setImagePath(imagePath);

                recipeService.updateRecipe(targetRecipe);
                JOptionPane.showMessageDialog(this, "Recipe updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            saved = true;
            dispose();
        } catch (RecipeValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
