package com.infobro.apaar.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "student_profile_update_requests")
public class StudentProfileUpdateRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @Column(name = "requester_email", nullable = false, length = 120) private String requesterEmail;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "requested_changes", nullable = false, columnDefinition = "jsonb") private Map<String, Object> requestedChanges;
    @Column(nullable = false, length = 16) private String status = "PENDING";
    @Column(name = "reviewer_email", length = 120) private String reviewerEmail;
    @Column(length = 1000) private String remarks;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "reviewed_at") private LocalDateTime reviewedAt;

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getRequesterEmail() { return requesterEmail; }
    public void setRequesterEmail(String requesterEmail) { this.requesterEmail = requesterEmail; }
    public Map<String, Object> getRequestedChanges() { return requestedChanges; }
    public void setRequestedChanges(Map<String, Object> requestedChanges) { this.requestedChanges = requestedChanges; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewerEmail() { return reviewerEmail; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
}
