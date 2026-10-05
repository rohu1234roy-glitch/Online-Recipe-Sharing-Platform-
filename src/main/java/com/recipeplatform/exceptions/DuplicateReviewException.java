package com.recipeplatform.exceptions;

// Thrown when an explorer attempts to submit multiple reviews for the same recipe
public class DuplicateReviewException extends Exception {
    public DuplicateReviewException(String message) {
        super(message);
    }
}
