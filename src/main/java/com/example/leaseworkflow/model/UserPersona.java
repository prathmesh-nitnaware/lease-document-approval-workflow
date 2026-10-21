package com.example.leaseworkflow.model;

import java.io.Serializable;

public class UserPersona implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String displayName;
    private String email;
    private String department;
    private UserRole role;
    private String avatarInitials;

    public UserPersona() {
    }

    public UserPersona(String id, String displayName, String email, String department, UserRole role, String avatarInitials) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.department = department;
        this.role = role;
        this.avatarInitials = avatarInitials;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getAvatarInitials() {
        return avatarInitials;
    }

    public void setAvatarInitials(String avatarInitials) {
        this.avatarInitials = avatarInitials;
    }

    public boolean isTenant() {
        return role == UserRole.TENANT;
    }

    public boolean isLegalReviewer() {
        return role == UserRole.LEGAL_REVIEWER;
    }

    public boolean isFinanceManager() {
        return role == UserRole.FINANCE_MANAGER;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean canReview() {
        return role != null && role.isReviewerOrHigher();
    }
}
