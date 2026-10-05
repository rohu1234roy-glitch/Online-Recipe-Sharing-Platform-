package com.recipeplatform.interfaces;

import com.recipeplatform.model.Recipe;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.RecipeValidationException;
import java.util.List;

// Interface for recipe operations (OOP Interface concept)
public interface RecipeOperations {
    boolean addRecipe(Recipe recipe) throws DatabaseException, RecipeValidationException;
    boolean updateRecipe(Recipe recipe) throws DatabaseException, RecipeValidationException;
    boolean deleteRecipe(int recipeId) throws DatabaseException;
    Recipe getRecipeById(int recipeId) throws DatabaseException;
    List<Recipe> getAllRecipes() throws DatabaseException;
}
