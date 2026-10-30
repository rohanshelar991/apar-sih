package com.infobro.apaar.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private String status; // VALID, PENDING, DUPLICATE, ERROR
    private final List<ValidationIssueInfo> issues = new ArrayList<>();

    public ValidationResult() {
        this.status = "VALID";
    }

    public void addIssue(ValidationIssueInfo issue) {
        issues.add(issue);
        updateStatus();
    }

    public void addIssue(String ruleCode, String severity, String message, String suggestedFix) {
        addIssue(new ValidationIssueInfo(ruleCode, severity, message, suggestedFix));
    }

    private void updateStatus() {
        boolean hasDuplicate = issues.stream().anyMatch(i ->
                ValidationRuleCode.DUPLICATE_IN_FILE.equals(i.getRuleCode()) ||
                ValidationRuleCode.DUPLICATE_SAME_INSTITUTION.equals(i.getRuleCode()) ||
                ValidationRuleCode.DUPLICATE_CROSS_INSTITUTION.equals(i.getRuleCode()));

        boolean hasError = issues.stream().anyMatch(i -> "ERROR".equalsIgnoreCase(i.getSeverity()));
        boolean hasWarning = issues.stream().anyMatch(i -> "WARNING".equalsIgnoreCase(i.getSeverity()));

        if (hasDuplicate) {
            this.status = "DUPLICATE";
        } else if (hasError) {
            this.status = "ERROR";
        } else if (hasWarning) {
            this.status = "PENDING";
        } else {
            this.status = "VALID";
        }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ValidationIssueInfo> getIssues() { return issues; }
    public boolean isValid() { return "VALID".equals(status); }
}
