package com.recipeplatform.dao;

import com.recipeplatform.database.DatabaseConnection;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.interfaces.CollectionOperations;
import com.recipeplatform.model.Recipe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Data Access Object for Saved Recipe Collections (Implements CollectionOperations & uses HashSet/ArrayList)
public class CollectionDAO implements CollectionOperations {

    @Override
    public boolean saveRecipe(int userId, int recipeId) throws DatabaseException {
        String sql = "INSERT OR IGNORE INTO saved_recipes (user_id, recipe_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, recipeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save recipe to collection: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeRecipe(int userId, int recipeId) throws DatabaseException {
        String sql = "DELETE FROM saved_recipes WHERE user_id = ? AND recipe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, recipeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to remove recipe from collection: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> getSavedRecipes(int userId) throws DatabaseException {
        List<Recipe> savedList = new ArrayList<>(); // Collections ArrayList requirement
        String sql = "SELECT r.*, u.name as contributor_name " +
                     "FROM saved_recipes sr " +
                     "JOIN recipes r ON sr.recipe_id = r.id " +
                     "JOIN users u ON r.contributor_id = u.id " +
                     "WHERE sr.user_id = ? ORDER BY sr.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    savedList.add(new Recipe(
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
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch saved collection: " + e.getMessage(), e);
        }
        return savedList;
    }

    @Override
    public Set<Integer> getSavedRecipeIds(int userId) throws DatabaseException {
        Set<Integer> savedIds = new HashSet<>(); // Collections HashSet requirement for O(1) checks
        String sql = "SELECT recipe_id FROM saved_recipes WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    savedIds.add(rs.getInt("recipe_id"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch saved recipe IDs: " + e.getMessage(), e);
        }
        return savedIds;
    }
}
