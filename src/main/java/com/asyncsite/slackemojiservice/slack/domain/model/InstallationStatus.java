package com.asyncsite.slackemojiservice.slack.domain.model;

public enum InstallationStatus {
    PENDING("Installation pending"),
    IN_PROGRESS("Installation in progress"),
    COMPLETED("Installation completed successfully"),
    PARTIAL("Installation partially completed with some failures"),
    FAILED("Installation failed"),
    CANCELLED("Installation cancelled by user");
    
    private final String description;
    
    InstallationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == PARTIAL;
    }
    
    public boolean isSuccess() {
        return this == COMPLETED;
    }
}