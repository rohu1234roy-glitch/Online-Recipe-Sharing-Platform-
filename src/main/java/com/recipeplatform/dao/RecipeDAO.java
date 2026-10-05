package com.recipeplatform.dao;

import com.recipeplatform.database.DatabaseConnection;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.RecipeValidationException;
import com.recipeplatform.interfaces.RecipeOperations;
import com.recipeplatform.model.Recipe;
import com.recipeplatform.util.ValidationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Data Access Object for Recipe CRUD & operations (Implements RecipeOperations interface)
public class RecipeDAO implements RecipeOperations {

    private Recipe mapResultSetToRecipe(ResultSet rs) throws SQLException {
        return new Recipe(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("ingredients"),
                rs.getString("instructions"),
                rs.getString("category"),
                rs.getString("image_path"),
                rs.getInt("contributor_id"),
                rs.getString("contributor_name"),
                rs.getString("status"),
                rs.getInt("views"),
                rs.getDouble("avg_rating"),
                rs.getInt("total_ratings"),
                rs.getString("created_at")
        );
    }

    @Override
    public boolean addRecipe(Recipe recipe) throws DatabaseException, RecipeValidationException {
        if (ValidationUtil.isEmpty(recipe.getTitle()) || ValidationUtil.isEmpty(recipe.getInstructions())) {
            throw new RecipeValidationException("Recipe title and instructions are required.");
        }

        String sql = "INSERT INTO recipes (title, description, ingredients, instructions, category, image_path, contributor_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, recipe.getTitle());
            ps.setString(2, recipe.getDescription());
            ps.setString(3, recipe.getIngredients());
            ps.setString(4, recipe.getInstructions());
            ps.setString(5, recipe.getCategory());
            ps.setString(6, recipe.getImagePath());
            ps.setInt(7, recipe.getContributorId());
            ps.setString(8, recipe.getStatus() != null ? recipe.getStatus() : "PENDING");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        recipe.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateRecipe(Recipe recipe) throws DatabaseException, RecipeValidationException {
        if (ValidationUtil.isEmpty(recipe.getTitle()) || ValidationUtil.isEmpty(recipe.getInstructions())) {
            throw new RecipeValidationException("Recipe title and instructions cannot be empty.");
        }

        String sql = "UPDATE recipes SET title = ?, description = ?, ingredients = ?, instructions = ?, category = ?, image_path = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, recipe.getTitle());
            ps.setString(2, recipe.getDescription());
            ps.setString(3, recipe.getIngredients());
            ps.setString(4, recipe.getInstructions());
            ps.setString(5, recipe.getCategory());
            ps.setString(6, recipe.getImagePath());
            ps.setString(7, recipe.getStatus());
            ps.setInt(8, recipe.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update recipe: " + e.getMessage(), e);
        }
    }

    // Uses Transaction Management (setAutoCommit(false), commit, rollback)
    @Override
    public boolean deleteRecipe(int recipeId) throws DatabaseException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // Step 1: Delete associated reviews
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM reviews WHERE recipe_id = ?")) {
                ps1.setInt(1, recipeId);
                ps1.executeUpdate();
            }

            // Step 2: Delete associated saved recipes
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM saved_recipes WHERE recipe_id = ?")) {
                ps2.setInt(1, recipeId);
                ps2.executeUpdate();
            }

            // Step 3: Delete recipe view logs
            try (PreparedStatement ps3 = conn.prepareStatement("DELETE FROM recipe_views WHERE recipe_id = ?")) {
                ps3.setInt(1, recipeId);
                ps3.executeUpdate();
            }

            // Step 4: Delete recipe record
            int deleted;
            try (PreparedStatement ps4 = conn.prepareStatement("DELETE FROM recipes WHERE id = ?")) {
                ps4.setInt(1, recipeId);
                deleted = ps4.executeUpdate();
            }

            conn.commit(); // Commit Transaction
            return deleted > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on failure
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw new DatabaseException("Transaction failed during recipe deletion: " + e.getMessage(), e);
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

    @Override
    public Recipe getRecipeById(int recipeId) throws DatabaseException {
        String sql = "SELECT r.*, u.name as contributor_name FROM recipes r JOIN users u ON r.contributor_id = u.id WHERE r.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recipeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRecipe(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch recipe: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Recipe> getAllRecipes() throws DatabaseException {
        List<Recipe> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name as contributor_name FROM recipes r JOIN users u ON r.contributor_id = u.id ORDER BY r.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToRecipe(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch recipes: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Recipe> getApprovedRecipes() throws DatabaseException {
        List<Recipe> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name as contributor_name FROM recipes r JOIN users u ON r.contributor_id = u.id WHERE r.status = 'APPROVED' ORDER BY r.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToRecipe(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch approved recipes: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Recipe> getPendingRecipes() throws DatabaseException {
        List<Recipe> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name as contributor_name FROM recipes r JOIN users u ON r.contributor_id = u.id WHERE r.status = 'PENDING' ORDER BY r.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToRecipe(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch pending recipes: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Recipe> getRecipesByContributor(int contributorId) throws DatabaseException {
        List<Recipe> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name as contributor_name FROM recipes r JOIN users u ON r.contributor_id = u.id WHERE r.contributor_id = ? ORDER BY r.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contributorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecipe(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch contributor recipes: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean updateRecipeStatus(int recipeId, String status) throws DatabaseException {
        String sql = "UPDATE recipes SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, recipeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update status: " + e.getMessage(), e);
        }
    }

    // Synchronized method to increment view count safely across concurrent user threads (Synchronization Rubric)
    public synchronized void incrementViewCount(int recipeId, int userId) throws DatabaseException {
        String updateViewsSql = "UPDATE recipes SET views = views + 1 WHERE id = ?";
        String logViewSql = "INSERT INTO recipe_views (recipe_id, user_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(updateViewsSql)) {
                ps1.setInt(1, recipeId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(logViewSql)) {
                ps2.setInt(1, recipeId);
                ps2.setInt(2, userId);
                ps2.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to log view: " + e.getMessage(), e);
        }
    }

    // Synchronized rating recalculation method to prevent race conditions when multiple users rate concurrently
    public synchronized void updateRatingStats(int recipeId, double avgRating, int totalRatings) throws DatabaseException {
        String sql = "UPDATE recipes SET avg_rating = ?, total_ratings = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, avgRating);
            ps.setInt(2, totalRatings);
            ps.setInt(3, recipeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update rating stats: " + e.getMessage(), e);
        }
    }
}
