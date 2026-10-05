package com.recipeplatform;

import com.recipeplatform.database.DatabaseInitializer;
import com.recipeplatform.gui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Main entry point for Online Recipe Sharing Platform Java GUI Application
public class Main {

    public static void main(String[] args) {
        // Initialize SQLite Database and seed sample data
        DatabaseInitializer.initialize();

        // Set System Look & Feel for clean native UI appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set System Look and Feel: " + e.getMessage());
        }

        // Launch Swing GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
