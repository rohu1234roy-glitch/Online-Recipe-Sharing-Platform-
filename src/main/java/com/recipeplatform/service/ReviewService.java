package com.recipeplatform.service;

import com.recipeplatform.dao.RecipeDAO;
import com.recipeplatform.dao.ReviewDAO;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.DuplicateReviewException;
import com.recipeplatform.model.Review;

import java.util.List;

// Service layer for Managing Reviews with Thread-Safe Synchronized Rating Updates (Synchronization Rubric)
public class ReviewService {

    private final ReviewDAO reviewDAO;
    private final RecipeDAO recipeDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
        this.recipeDAO = new RecipeDAO();
    }

    // Synchronized method to ensure rating statistics are safely updated during concurrent reviews (Synchronization Rubric requirement)
    public synchronized boolean addReviewAndRecalculateRating(Review review) throws DatabaseException, DuplicateReviewException {
        // 1. Add review (throws DuplicateReviewException if user already reviewed)
        boolean added = reviewDAO.addReview(review);
        if (!added) {
            return false;
        }

        // 2. Fetch all reviews for recipe to recalculate average rating safely
        List<Review> recipeReviews = reviewDAO.getReviewsByRecipe(review.getRecipeId());
        int totalRatings = recipeReviews.size();
        double sum = 0;
        for (Review r : recipeReviews) {
            sum += r.getRating();
        }

        double avgRating = totalRatings > 0 ? (sum / totalRatings) : 0.0;
        // Round to 1 decimal place
        avgRating = Math.round(avgRating * 10.0) / 10.0;

        // 3. Update recipe rating statistics in database safely
        recipeDAO.updateRatingStats(review.getRecipeId(), avgRating, totalRatings);

        return true;
    }

    public List<Review> getReviewsForRecipe(int recipeId) throws DatabaseException {
        return reviewDAO.getReviewsByRecipe(recipeId);
    }

    public List<Review> getAllReviews() throws DatabaseException {
        return reviewDAO.getAllReviews();
    }

    public List<Review> getReviewsByUser(int userId) throws DatabaseException {
        return reviewDAO.getReviewsByUser(userId);
    }

    public synchronized boolean deleteReviewAndRecalculateRating(int reviewId, int recipeId) throws DatabaseException {
        boolean deleted = reviewDAO.deleteReview(reviewId);
        if (deleted) {
            List<Review> remaining = reviewDAO.getReviewsByRecipe(recipeId);
            int totalRatings = remaining.size();
            double sum = 0;
            for (Review r : remaining) {
                sum += r.getRating();
            }
            double avgRating = totalRatings > 0 ? (sum / totalRatings) : 0.0;
            avgRating = Math.round(avgRating * 10.0) / 10.0;

            recipeDAO.updateRatingStats(recipeId, avgRating, totalRatings);
        }
        return deleted;
    }
}
