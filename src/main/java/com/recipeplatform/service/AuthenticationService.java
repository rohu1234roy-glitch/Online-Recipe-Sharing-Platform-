package com.recipeplatform.service;

import com.recipeplatform.dao.UserDAO;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.InvalidLoginException;
import com.recipeplatform.model.Admin;
import com.recipeplatform.model.RecipeContributor;
import com.recipeplatform.model.RecipeExplorer;
import com.recipeplatform.model.User;
import com.recipeplatform.util.ValidationUtil;

// Authentication service handling user login and registration
public class AuthenticationService {

    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public User login(String email, String password) throws InvalidLoginException, DatabaseException {
        if (ValidationUtil.isEmpty(email) || ValidationUtil.isEmpty(password)) {
            throw new InvalidLoginException("Email and password cannot be empty.");
        }

        User user = userDAO.getUserByEmail(email.trim());
        if (user == null || !user.getPassword().equals(password)) {
            throw new InvalidLoginException("Invalid email or password.");
        }

        return user;
    }

    public boolean register(String name, String email, String password, String role) throws InvalidLoginException, DatabaseException {
        if (ValidationUtil.isEmpty(name)) {
            throw new InvalidLoginException("Full name is required.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new InvalidLoginException("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new InvalidLoginException("Password must be at least 4 characters long.");
        }

        User existing = userDAO.getUserByEmail(email.trim());
        if (existing != null) {
            throw new InvalidLoginException("An account with this email already exists.");
        }

        User newUser;
        if ("Admin".equalsIgnoreCase(role)) {
            newUser = new Admin(name.trim(), email.trim(), password);
        } else if ("Contributor".equalsIgnoreCase(role)) {
            newUser = new RecipeContributor(name.trim(), email.trim(), password);
        } else {
            newUser = new RecipeExplorer(name.trim(), email.trim(), password);
        }

        return userDAO.addUser(newUser);
    }
}
