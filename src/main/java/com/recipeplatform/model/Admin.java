package com.recipeplatform.model;

import com.recipeplatform.gui.AdminDashboard;
import javax.swing.JFrame;

// Admin user extending base User
public class Admin extends User {

    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password, "Admin");
    }

    public Admin(String name, String email, String password) {
        super(name, email, password, "Admin");
    }

    @Override
    public void showDashboard(JFrame currentFrame) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        AdminDashboard dashboard = new AdminDashboard(this);
        dashboard.setVisible(true);
    }
}
