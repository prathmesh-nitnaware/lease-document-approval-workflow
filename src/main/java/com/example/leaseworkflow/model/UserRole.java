package com.example.leaseworkflow.model;

public enum UserRole {
    TENANT("Tenant / Requester", "Can submit lease requests, track status, and upload revisions.", "badge-tenant", "#38bdf8"),
    LEGAL_REVIEWER("Legal Reviewer", "Verifies lease agreements, title deeds, and regulatory compliance.", "badge-legal", "#c084fc"),
    FINANCE_MANAGER("Finance Manager", "Audits financial terms, security deposits, and gives executive approval.", "badge-finance", "#34d399"),
    ADMIN("Administrator", "Platform administration, user governance, and compliance audit trail.", "badge-admin", "#f43f5e");

    private final String displayName;
    private final String description;
    private final String badgeClass;
    private final String themeColor;

    UserRole(String displayName, String description, String badgeClass, String themeColor) {
        this.displayName = displayName;
        this.description = description;
        this.badgeClass = badgeClass;
        this.themeColor = themeColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public boolean isReviewerOrHigher() {
        return this == LEGAL_REVIEWER || this == FINANCE_MANAGER || this == ADMIN;
    }
}
