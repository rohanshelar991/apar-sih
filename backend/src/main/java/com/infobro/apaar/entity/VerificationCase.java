package com.infobro.apaar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_cases")
public class VerificationCase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "institution_id", nullable = false) private Institution institution;
    @Column(name="submitted_by", nullable=false, length=120) private String submittedBy;
    @Column(nullable=false, length=24) private String status="PENDING";
    @Column(name="reviewer_email", length=120) private String reviewerEmail;
    @Column(length=12, nullable=false) private String priority="NORMAL";
    @Column(name="assigned_to", length=120) private String assignedTo;
    @Column(length=1000) private String remarks;
    @Column(name="submitted_at", nullable=false) private LocalDateTime submittedAt=LocalDateTime.now();
    @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt=LocalDateTime.now();
    public Long getId(){return id;} public Student getStudent(){return student;} public Institution getInstitution(){return institution;}
    public String getSubmittedBy(){return submittedBy;} public String getStatus(){return status;} public String getReviewerEmail(){return reviewerEmail;}
    public String getRemarks(){return remarks;} public LocalDateTime getSubmittedAt(){return submittedAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
    public String getPriority(){return priority;} public String getAssignedTo(){return assignedTo;}
    public void setStudent(Student v){student=v;} public void setInstitution(Institution v){institution=v;} public void setSubmittedBy(String v){submittedBy=v;}
    public void setStatus(String v){status=v;} public void setReviewerEmail(String v){reviewerEmail=v;} public void setRemarks(String v){remarks=v;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
    public void setPriority(String v){priority=v;} public void setAssignedTo(String v){assignedTo=v;}
}
