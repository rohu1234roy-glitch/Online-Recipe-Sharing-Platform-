package com.recipeplatform.model;

import com.recipeplatform.gui.ExplorerDashboard;
import javax.swing.JFrame;

// Recipe Explorer extending base User
public class RecipeExplorer extends User {

    public RecipeExplorer(int id, String name, String email, String password) {
        super(id, name, email, password, "Explorer");
    }

    public RecipeExplorer(String name, String email, String password) {
        super(name, email, password, "Explorer");
    }

    @Override
    public void showDashboard(JFrame currentFrame) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        ExplorerDashboard dashboard = new ExplorerDashboard(this);
        dashboard.setVisible(true);
    }
}
