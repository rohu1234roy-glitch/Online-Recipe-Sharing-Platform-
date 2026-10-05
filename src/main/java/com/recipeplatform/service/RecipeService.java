package com.recipeplatform.service;

import com.recipeplatform.dao.RecipeDAO;
import com.recipeplatform.exceptions.DatabaseException;
import com.recipeplatform.exceptions.RecipeValidationException;
import com.recipeplatform.model.Recipe;
import com.recipeplatform.util.GenericRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Service layer for Recipes demonstrating Collections (HashMap), Generics & Multithreading
public class RecipeService {

    private final RecipeDAO recipeDAO;
    private final GenericRepository<Recipe> genericRepo;

    public RecipeService() {
        this.recipeDAO = new RecipeDAO();
        this.genericRepo = new GenericRepository<>();
    }

    // Creates a HashMap of approved recipes by ID for quick O(1) lookups (Collections HashMap requirement)
    public Map<Integer, Recipe> getApprovedRecipesMap() throws DatabaseException {
        List<Recipe> recipes = recipeDAO.getApprovedRecipes();
        Map<Integer, Recipe> recipeMap = new HashMap<>(); // Collections HashMap
        for (Recipe r : recipes) {
            recipeMap.put(r.getId(), r);
        }
        return recipeMap;
    }

    public List<Recipe> getApprovedRecipes() throws DatabaseException {
        return recipeDAO.getApprovedRecipes();
    }

    public List<Recipe> getPendingRecipes() throws DatabaseException {
        return recipeDAO.getPendingRecipes();
    }

    public List<Recipe> getContributorRecipes(int contributorId) throws DatabaseException {
        return recipeDAO.getRecipesByContributor(contributorId);
    }

    public boolean addRecipe(Recipe recipe) throws DatabaseException, RecipeValidationException {
        return recipeDAO.addRecipe(recipe);
    }

    public boolean updateRecipe(Recipe recipe) throws DatabaseException, RecipeValidationException {
        return recipeDAO.updateRecipe(recipe);
    }

    public boolean deleteRecipe(int recipeId) throws DatabaseException {
        return recipeDAO.deleteRecipe(recipeId);
    }

    public boolean approveRecipe(int recipeId) throws DatabaseException {
        return recipeDAO.updateRecipeStatus(recipeId, "APPROVED");
    }

    public boolean rejectRecipe(int recipeId) throws DatabaseException {
        return recipeDAO.updateRecipeStatus(recipeId, "REJECTED");
    }

    public Recipe getRecipeById(int recipeId) throws DatabaseException {
        return recipeDAO.getRecipeById(recipeId);
    }

    public void incrementViews(int recipeId, int userId) throws DatabaseException {
        recipeDAO.incrementViewCount(recipeId, userId);
    }

    // Search and filter approved recipes using GenericRepository (Generics requirement)
    public List<Recipe> searchAndFilterRecipes(String query, String category) throws DatabaseException {
        List<Recipe> allApproved = recipeDAO.getApprovedRecipes();

        return genericRepo.filter(allApproved, recipe -> {
            boolean matchesQuery = true;
            if (query != null && !query.trim().isEmpty()) {
                String q = query.trim().toLowerCase();
                matchesQuery = recipe.getTitle().toLowerCase().contains(q) ||
                               recipe.getDescription().toLowerCase().contains(q) ||
                               recipe.getContributorName().toLowerCase().contains(q);
            }

            boolean matchesCategory = true;
            if (category != null && !category.equals("All Categories") && !category.trim().isEmpty()) {
                matchesCategory = recipe.getCategory().equalsIgnoreCase(category.trim());
            }

            return matchesQuery && matchesCategory;
        });
    }

    // Class to hold calculated statistics for dashboards
    public static class ContributorStats {
        public int totalRecipes;
        public int approvedCount;
        public int pendingCount;
        public int totalViews;
        public double averageRating;
    }

    // Loads contributor statistics in a background thread to prevent UI freezing (Multithreading requirement)
    public void loadContributorStatsAsync(int contributorId, StatsCallback callback) {
        new Thread(() -> {
            try {
                List<Recipe> recipes = recipeDAO.getRecipesByContributor(contributorId);
                ContributorStats stats = new ContributorStats();
                stats.totalRecipes = recipes.size();

                double ratingSum = 0;
                int ratedCount = 0;

                for (Recipe r : recipes) {
                    if ("APPROVED".equalsIgnoreCase(r.getStatus())) {
                        stats.approvedCount++;
                    } else if ("PENDING".equalsIgnoreCase(r.getStatus())) {
                        stats.pendingCount++;
                    }
                    stats.totalViews += r.getViews();

                    if (r.getTotalRatings() > 0) {
                        ratingSum += r.getAvgRating();
                        ratedCount++;
                    }
                }

                stats.averageRating = ratedCount > 0 ? (ratingSum / ratedCount) : 0.0;

                // Deliver result back on Swing thread
                javax.swing.SwingUtilities.invokeLater(() -> callback.onSuccess(stats));
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    public interface StatsCallback {
        void onSuccess(ContributorStats stats);
        void onError(String errorMessage);
    }
}
