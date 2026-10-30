package com.infobro.apaar.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_apaar", columnList = "apaar_id"),
    @Index(name = "idx_student_inst", columnList = "institution_id"),
    @Index(name = "idx_student_prog", columnList = "programme_id"),
    @Index(name = "idx_student_status", columnList = "verification_status"),
    @Index(name = "idx_student_year", columnList = "admission_year")
})
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apaar_id", nullable = false, length = 16)
    private String apaarId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false, length = 16)
    private String gender = "Other";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(nullable = false, length = 32)
    private String category = "REGULAR";

    @Column(length = 120)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "verification_status", nullable = false, length = 24)
    private String verificationStatus = "PENDING";

    @Column(name = "consent_given", nullable = false)
    private Boolean consentGiven = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Student() {}

    public Student(String apaarId, String name, LocalDate dob, String gender, Programme programme,
                   Institution institution, Integer admissionYear, String category, String email,
                   String phone, String verificationStatus, Boolean consentGiven) {
        this.apaarId = apaarId;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.programme = programme;
        this.institution = institution;
        this.admissionYear = admissionYear;
        this.category = category;
        this.email = email;
        this.phone = phone;
        this.verificationStatus = verificationStatus;
        this.consentGiven = consentGiven != null ? consentGiven : false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApaarId() { return apaarId; }
    public void setApaarId(String apaarId) { this.apaarId = apaarId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Programme getProgramme() { return programme; }
    public void setProgramme(Programme programme) { this.programme = programme; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Boolean getConsentGiven() { return consentGiven; }
    public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Mask APAAR ID for privacy (e.g., "12XX XXXX 4567")
     */
    public String getMaskedApaarId() {
        if (apaarId == null || apaarId.length() < 12) {
            return apaarId;
        }
        String clean = apaarId.replaceAll("\\s+", "");
        if (clean.length() == 12) {
            return clean.substring(0, 2) + "XX XXXX " + clean.substring(8);
        }
        return apaarId;
    }
}
