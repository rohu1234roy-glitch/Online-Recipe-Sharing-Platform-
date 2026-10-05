package com.recipeplatform.interfaces;

import com.recipeplatform.model.Recipe;
import com.recipeplatform.exceptions.DatabaseException;
import java.util.List;
import java.util.Set;

// Interface for personal recipe collection management (OOP Interface concept)
public interface CollectionOperations {
    boolean saveRecipe(int userId, int recipeId) throws DatabaseException;
    boolean removeRecipe(int userId, int recipeId) throws DatabaseException;
    List<Recipe> getSavedRecipes(int userId) throws DatabaseException;
    Set<Integer> getSavedRecipeIds(int userId) throws DatabaseException;
}
