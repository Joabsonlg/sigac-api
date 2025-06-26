package io.github.joabsonlg.sigac_api.common.model;

public class Employee {
    private User user;
    private String role;

    // Getters and setters

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
