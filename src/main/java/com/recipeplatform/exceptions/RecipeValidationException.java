package com.recipeplatform.exceptions;

// Exception thrown when recipe input validation fails
public class RecipeValidationException extends Exception {
    public RecipeValidationException(String message) {
        super(message);
    }
}
