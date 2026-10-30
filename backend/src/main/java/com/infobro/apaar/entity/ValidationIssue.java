package com.infobro.apaar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_issues", indexes = {
    @Index(name = "idx_issue_student", columnList = "student_id"),
    @Index(name = "idx_issue_batch", columnList = "upload_batch_id"),
    @Index(name = "idx_issue_status", columnList = "status"),
    @Index(name = "idx_issue_rule", columnList = "rule_code")
})
public class ValidationIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_batch_id")
    private UploadBatch uploadBatch;

    @Column(name = "rule_code", nullable = false, length = 64)
    private String ruleCode;

    @Column(nullable = false, length = 16)
    private String severity = "ERROR";

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "suggested_fix", length = 500)
    private String suggestedFix;

    @Column(nullable = false, length = 16)
    private String status = "OPEN";

    @Column(name = "resolved_by", length = 120)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "raw_record", length = 1000)
    private String rawRecord;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ValidationIssue() {}

    public ValidationIssue(Student student, UploadBatch uploadBatch, String ruleCode, String severity,
                           String message, String suggestedFix, String rawRecord) {
        this.student = student;
        this.uploadBatch = uploadBatch;
        this.ruleCode = ruleCode;
        this.severity = severity;
        this.message = message;
        this.suggestedFix = suggestedFix;
        this.rawRecord = rawRecord;
        this.status = "OPEN";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public UploadBatch getUploadBatch() { return uploadBatch; }
    public void setUploadBatch(UploadBatch uploadBatch) { this.uploadBatch = uploadBatch; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSuggestedFix() { return suggestedFix; }
    public void setSuggestedFix(String suggestedFix) { this.suggestedFix = suggestedFix; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getRawRecord() { return rawRecord; }
    public void setRawRecord(String rawRecord) { this.rawRecord = rawRecord; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
