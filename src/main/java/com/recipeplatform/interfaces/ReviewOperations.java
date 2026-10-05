package com.recipeplatform.interfaces;

import com.recipeplatform.model.Review;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.DuplicateReviewException;
import java.util.List;

// Interface for review management (OOP Interface concept)
public interface ReviewOperations {
    boolean addReview(Review review) throws DatabaseException, DuplicateReviewException;
    List<Review> getReviewsByRecipe(int recipeId) throws DatabaseException;
    boolean deleteReview(int reviewId) throws DatabaseException;
}
