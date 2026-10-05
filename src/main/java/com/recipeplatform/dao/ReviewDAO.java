package com.recipeplatform.dao;

import com.recipeplatform.database.DatabaseConnection;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.DuplicateReviewException;
import com.recipeplatform.interfaces.ReviewOperations;
import com.recipeplatform.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Data Access Object for Recipe Reviews (Implements ReviewOperations)
public class ReviewDAO implements ReviewOperations {

    public boolean hasUserReviewed(int recipeId, int userId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM reviews WHERE recipe_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recipeId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking existing review: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean addReview(Review review) throws DatabaseException, DuplicateReviewException {
        if (hasUserReviewed(review.getRecipeId(), review.getUserId())) {
            throw new DuplicateReviewException("You have already submitted a review for this recipe.");
        }

        String sql = "INSERT INTO reviews (recipe_id, user_id, rating, review_text) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, review.getRecipeId());
            ps.setInt(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getReviewText());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        review.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add review: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Review> getReviewsByRecipe(int recipeId) throws DatabaseException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name, rec.title as recipe_title " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN recipes rec ON r.recipe_id = rec.id " +
                     "WHERE r.recipe_id = ? ORDER BY r.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recipeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Review(
                            rs.getInt("id"),
                            rs.getInt("recipe_id"),
                            rs.getString("recipe_title"),
                            rs.getInt("user_id"),
                            rs.getString("user_name"),
                            rs.getInt("rating"),
                            rs.getString("review_text"),
                            rs.getString("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch reviews: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Review> getAllReviews() throws DatabaseException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name, rec.title as recipe_title " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN recipes rec ON r.recipe_id = rec.id " +
                     "ORDER BY r.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Review(
                        rs.getInt("id"),
                        rs.getInt("recipe_id"),
                        rs.getString("recipe_title"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getInt("rating"),
                        rs.getString("review_text"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all reviews: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Review> getReviewsByUser(int userId) throws DatabaseException {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name, rec.title as recipe_title " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN recipes rec ON r.recipe_id = rec.id " +
                     "WHERE r.user_id = ? ORDER BY r.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Review(
                            rs.getInt("id"),
                            rs.getInt("recipe_id"),
                            rs.getString("recipe_title"),
                            rs.getInt("user_id"),
                            rs.getString("user_name"),
                            rs.getInt("rating"),
                            rs.getString("review_text"),
                            rs.getString("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch user reviews: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean deleteReview(int reviewId) throws DatabaseException {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete review: " + e.getMessage(), e);
        }
    }
}
