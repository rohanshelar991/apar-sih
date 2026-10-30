package com.infobro.apaar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens", indexes = {
    @Index(name = "idx_token_value", columnList = "token", unique = true),
    @Index(name = "idx_token_student", columnList = "student_id")
})
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "allowed_fields", nullable = false, length = 255)
    private String allowedFields; // e.g. "NAME,DOB,INSTITUTION,PROGRAMME,ADMISSION_YEAR,STATUS"

    @Column(name = "created_by", nullable = false, length = 120)
    private String createdBy;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public VerificationToken() {}

    public VerificationToken(Student student, String token, LocalDateTime expiresAt, String allowedFields, String createdBy) {
        this.student = student;
        this.token = token;
        this.expiresAt = expiresAt;
        this.allowedFields = allowedFields;
        this.createdBy = createdBy;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getAllowedFields() { return allowedFields; }
    public void setAllowedFields(String allowedFields) { this.allowedFields = allowedFields; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
