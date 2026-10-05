package com.recipeplatform.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Manages SQLite JDBC Database Connection (JDBC Requirement)
public class DatabaseConnection {

    private static final String DB_DIR = "database";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIR + "/recipe_platform.db";

    static {
        try {
            // Explicitly load SQLite JDBC Driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    // Get a new database connection
    public static Connection getConnection() throws SQLException {
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return DriverManager.getConnection(DB_URL);
    }
}
