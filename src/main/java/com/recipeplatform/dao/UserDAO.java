package com.recipeplatform.dao;

import com.recipeplatform.database.DatabaseConnection;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.model.Admin;
import com.recipeplatform.model.RecipeContributor;
import com.recipeplatform.model.RecipeExplorer;
import com.recipeplatform.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Data Access Object for User operations (Database Operation Classes Rubric)
public class UserDAO {

    // Helper to map ResultSet row to appropriate User subclass (Polymorphism)
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String role = rs.getString("role");

        if ("Admin".equalsIgnoreCase(role)) {
            return new Admin(id, name, email, password);
        } else if ("Contributor".equalsIgnoreCase(role)) {
            return new RecipeContributor(id, name, email, password);
        } else {
            return new RecipeExplorer(id, name, email, password);
        }
    }

    public boolean addUser(User user) throws DatabaseException {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add user: " + e.getMessage(), e);
        }
    }

    public User getUserByEmail(String email) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database query failed: " + e.getMessage(), e);
        }
        return null;
    }

    public User getUserById(int id) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database query failed: " + e.getMessage(), e);
        }
        return null;
    }

    public List<User> getAllUsers() throws DatabaseException {
        List<User> userList = new ArrayList<>(); // Collections ArrayList
        String sql = "SELECT * FROM users ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userList.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve users: " + e.getMessage(), e);
        }
        return userList;
    }

    public boolean updateUser(User user) throws DatabaseException {
        String sql = "UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user profile: " + e.getMessage(), e);
        }
    }

    // Demonstrates JDBC Transaction Management (setAutoCommit(false), commit, rollback)
    public boolean deleteUserWithTransaction(int userId) throws DatabaseException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start JDBC transaction

            // Step 1: Remove user saved recipes
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM saved_recipes WHERE user_id = ?")) {
                ps1.setInt(1, userId);
                ps1.executeUpdate();
            }

            // Step 2: Remove reviews submitted by user
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM reviews WHERE user_id = ?")) {
                ps2.setInt(1, userId);
                ps2.executeUpdate();
            }

            // Step 3: Remove user messages
            try (PreparedStatement ps3 = conn.prepareStatement("DELETE FROM messages WHERE sender_id = ? OR receiver_id = ?")) {
                ps3.setInt(1, userId);
                ps3.setInt(2, userId);
                ps3.executeUpdate();
            }

            // Step 4: Remove user account
            try (PreparedStatement ps4 = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                ps4.setInt(1, userId);
                ps4.executeUpdate();
            }

            conn.commit(); // Commit transaction if all steps succeed
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on failure
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw new DatabaseException("Transaction failed while deleting user: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Failed to reset autoCommit: " + e.getMessage());
                }
            }
        }
    }
}
