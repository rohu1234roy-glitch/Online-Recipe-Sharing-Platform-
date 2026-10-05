package com.recipeplatform.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Automatically initializes database tables and seeds demo sample data
public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable Foreign Key support in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL" +
                    ");");

            // 2. Recipes Table
            stmt.execute("CREATE TABLE IF NOT EXISTS recipes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "ingredients TEXT NOT NULL, " +
                    "instructions TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "image_path TEXT, " +
                    "contributor_id INTEGER NOT NULL, " +
                    "status TEXT DEFAULT 'PENDING', " +
                    "views INTEGER DEFAULT 0, " +
                    "avg_rating REAL DEFAULT 0.0, " +
                    "total_ratings INTEGER DEFAULT 0, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (contributor_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // 3. Reviews Table
            stmt.execute("CREATE TABLE IF NOT EXISTS reviews (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "recipe_id INTEGER NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "rating INTEGER NOT NULL, " +
                    "review_text TEXT, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "UNIQUE(recipe_id, user_id)" +
                    ");");

            // 4. Saved Recipes Table
            stmt.execute("CREATE TABLE IF NOT EXISTS saved_recipes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "recipe_id INTEGER NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE, " +
                    "UNIQUE(user_id, recipe_id)" +
                    ");");

            // 5. Messages Table
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender_id INTEGER NOT NULL, " +
                    "receiver_id INTEGER NOT NULL, " +
                    "message_text TEXT NOT NULL, " +
                    "sent_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // 6. Recipe Views Table
            stmt.execute("CREATE TABLE IF NOT EXISTS recipe_views (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "recipe_id INTEGER NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "viewed_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // 7. System Settings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS system_settings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "setting_name TEXT UNIQUE NOT NULL, " +
                    "setting_value TEXT NOT NULL" +
                    ");");

            // Seed initial data if empty
            seedInitialData(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        // Check if users exist
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert default users
                String insertUser = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                    // Admin
                    ps.setString(1, "System Admin");
                    ps.setString(2, "admin@recipe.com");
                    ps.setString(3, "admin123");
                    ps.setString(4, "Admin");
                    ps.executeUpdate();

                    // Contributor
                    ps.setString(1, "Chef Rahul");
                    ps.setString(2, "contributor@recipe.com");
                    ps.setString(3, "contrib123");
                    ps.setString(4, "Contributor");
                    ps.executeUpdate();

                    // Explorer
                    ps.setString(1, "Ananya Sharma");
                    ps.setString(2, "explorer@recipe.com");
                    ps.setString(3, "explorer123");
                    ps.setString(4, "Explorer");
                    ps.executeUpdate();
                }

                // Insert sample recipes for Chef Rahul (id 2)
                String insertRecipe = "INSERT INTO recipes (title, description, ingredients, instructions, category, contributor_id, status, views, avg_rating, total_ratings) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertRecipe)) {
                    // Recipe 1: Paneer Butter Masala
                    ps.setString(1, "Paneer Butter Masala");
                    ps.setString(2, "Rich and creamy curry made with paneer, butter, tomatoes, and cashews.");
                    ps.setString(3, "200g Paneer, 2 tbsp Butter, 3 Tomatoes, 10 Cashews, 1 tsp Garam Masala, Cream, Salt");
                    ps.setString(4, "1. Puree tomatoes and cashews.\n2. Melt butter in pan, add puree and spices.\n3. Simmer for 10 mins, add paneer cubes and cream.\n4. Serve hot with Naan.");
                    ps.setString(5, "Main Course");
                    ps.setInt(6, 2); // Contributor Rahul
                    ps.setString(7, "APPROVED");
                    ps.setInt(8, 45);
                    ps.setDouble(9, 4.8);
                    ps.setInt(10, 5);
                    ps.executeUpdate();

                    // Recipe 2: Masala Dosa
                    ps.setString(1, "Crispy Masala Dosa");
                    ps.setString(2, "Traditional South Indian fermented crepe stuffed with spiced potato filling.");
                    ps.setString(3, "Dosa Batter, 3 Potatoes, 1 Onion, Mustard seeds, Curry leaves, Turmeric, Oil");
                    ps.setString(4, "1. Boil potatoes and mash with sauted onions, mustard seeds, turmeric.\n2. Spread dosa batter thinly on hot tawa.\n3. Add oil, place potato filling in center and roll.");
                    ps.setString(5, "Breakfast");
                    ps.setInt(6, 2);
                    ps.setString(7, "APPROVED");
                    ps.setInt(8, 30);
                    ps.setDouble(9, 4.5);
                    ps.setInt(10, 2);
                    ps.executeUpdate();

                    // Recipe 3: Veg Biryani
                    ps.setString(1, "Hyderabadi Veg Biryani");
                    ps.setString(2, "Aromatic rice dish cooked with layered vegetables and rich spices.");
                    ps.setString(3, "2 cups Basmati Rice, Mixed Vegetables, Biryani Masala, Saffron, Ghee, Fried Onions");
                    ps.setString(4, "1. Parboil rice with whole spices.\n2. Cook spiced vegetable gravy.\n3. Layer rice and gravy, top with saffron water & ghee.\n4. Dum cook for 20 mins.");
                    ps.setString(5, "Main Course");
                    ps.setInt(6, 2);
                    ps.setString(7, "PENDING"); // Pending admin approval
                    ps.setInt(8, 10);
                    ps.setDouble(9, 0.0);
                    ps.setInt(10, 0);
                    ps.executeUpdate();
                }

                // Insert sample system settings
                String insertSetting = "INSERT INTO system_settings (setting_name, setting_value) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSetting)) {
                    ps.setString(1, "Allow_Guest_Browsing");
                    ps.setString(2, "false");
                    ps.executeUpdate();

                    ps.setString(1, "Auto_Approve_Recipes");
                    ps.setString(2, "false");
                    ps.executeUpdate();

                    ps.setString(1, "Platform_Name");
                    ps.setString(2, "College Recipe Sharing Platform");
                    ps.executeUpdate();
                }
            }
        }
    }
}
