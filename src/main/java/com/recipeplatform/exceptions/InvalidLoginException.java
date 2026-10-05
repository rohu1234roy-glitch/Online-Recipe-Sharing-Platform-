package com.recipeplatform.exceptions;

// Exception thrown when login authentication fails
public class InvalidLoginException extends Exception {
    public InvalidLoginException(String message) {
        super(message);
    }
}
