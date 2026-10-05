package com.recipeplatform.model;

import com.recipeplatform.gui.ContributorDashboard;
import javax.swing.JFrame;

// Recipe Contributor extending base User
public class RecipeContributor extends User {

    public RecipeContributor(int id, String name, String email, String password) {
        super(id, name, email, password, "Contributor");
    }

    public RecipeContributor(String name, String email, String password) {
        super(name, email, password, "Contributor");
    }

    @Override
    public void showDashboard(JFrame currentFrame) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        ContributorDashboard dashboard = new ContributorDashboard(this);
        dashboard.setVisible(true);
    }
}
