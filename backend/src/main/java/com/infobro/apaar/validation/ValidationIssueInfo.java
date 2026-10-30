package com.infobro.apaar.validation;

public class ValidationIssueInfo {
    private final String ruleCode;
    private final String severity; // ERROR, WARNING, INFO
    private final String message;
    private final String suggestedFix;

    public ValidationIssueInfo(String ruleCode, String severity, String message, String suggestedFix) {
        this.ruleCode = ruleCode;
        this.severity = severity;
        this.message = message;
        this.suggestedFix = suggestedFix;
    }

    public String getRuleCode() { return ruleCode; }
    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public String getSuggestedFix() { return suggestedFix; }
}
