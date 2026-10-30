package com.infobro.apaar.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DTOs {

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private String token;
        private String tokenType = "Bearer";
        private String email;
        private String fullName;
        private String role;
        private Long institutionId;
        private String institutionCode;
        private String institutionName;

        public LoginResponse(String token, String email, String fullName, String role,
                             Long institutionId, String institutionCode, String institutionName) {
            this.token = token;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.institutionId = institutionId;
            this.institutionCode = institutionCode;
            this.institutionName = institutionName;
        }

        public String getToken() { return token; }
        public String getTokenType() { return tokenType; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
        public Long getInstitutionId() { return institutionId; }
        public String getInstitutionCode() { return institutionCode; }
        public String getInstitutionName() { return institutionName; }
    }

    public static class StudentDto {
        private Long id;
        private String apaarId;
        private String maskedApaarId;
        private String name;
        private LocalDate dob;
        private String gender;
        private Long programmeId;
        private String programmeName;
        private String programmeCode;
        private Long institutionId;
        private String institutionCode;
        private String institutionName;
        private Integer admissionYear;
        private String category;
        private String email;
        private String phone;
        private String verificationStatus;
        private Boolean consentGiven;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<ValidationIssueDto> issues;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getApaarId() { return apaarId; }
        public void setApaarId(String apaarId) { this.apaarId = apaarId; }
        public String getMaskedApaarId() { return maskedApaarId; }
        public void setMaskedApaarId(String maskedApaarId) { this.maskedApaarId = maskedApaarId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public LocalDate getDob() { return dob; }
        public void setDob(LocalDate dob) { this.dob = dob; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public Long getProgrammeId() { return programmeId; }
        public void setProgrammeId(Long programmeId) { this.programmeId = programmeId; }
        public String getProgrammeName() { return programmeName; }
        public void setProgrammeName(String programmeName) { this.programmeName = programmeName; }
        public String getProgrammeCode() { return programmeCode; }
        public void setProgrammeCode(String programmeCode) { this.programmeCode = programmeCode; }
        public Long getInstitutionId() { return institutionId; }
        public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }
        public String getInstitutionCode() { return institutionCode; }
        public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }
        public String getInstitutionName() { return institutionName; }
        public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
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
        public List<ValidationIssueDto> getIssues() { return issues; }
        public void setIssues(List<ValidationIssueDto> issues) { this.issues = issues; }
    }

    public static class StudentFormRequest {
        private String apaarId;
        private String name;
        private LocalDate dob;
        private String gender;
        private Long programmeId;
        private Integer admissionYear;
        private String category;
        private String email;
        private String phone;
        private Boolean consentGiven;

        public String getApaarId() { return apaarId; }
        public void setApaarId(String apaarId) { this.apaarId = apaarId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public LocalDate getDob() { return dob; }
        public void setDob(LocalDate dob) { this.dob = dob; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public Long getProgrammeId() { return programmeId; }
        public void setProgrammeId(Long programmeId) { this.programmeId = programmeId; }
        public Integer getAdmissionYear() { return admissionYear; }
        public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Boolean getConsentGiven() { return consentGiven; }
        public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }
    }

    public static class ValidationIssueDto {
        private Long id;
        private Long studentId;
        private String studentName;
        private String apaarId;
        private String maskedApaarId;
        private Long batchId;
        private String ruleCode;
        private String severity;
        private String message;
        private String suggestedFix;
        private String status;
        private String resolvedBy;
        private LocalDateTime resolvedAt;
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getApaarId() { return apaarId; }
        public void setApaarId(String apaarId) { this.apaarId = apaarId; }
        public String getMaskedApaarId() { return maskedApaarId; }
        public void setMaskedApaarId(String maskedApaarId) { this.maskedApaarId = maskedApaarId; }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }
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
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class IssueResolveRequest {
        private String apaarId;
        private String name;
        private LocalDate dob;
        private Long programmeId;
        private Integer admissionYear;
        private String category;
        private String resolutionNote;

        public String getApaarId() { return apaarId; }
        public void setApaarId(String apaarId) { this.apaarId = apaarId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public LocalDate getDob() { return dob; }
        public void setDob(LocalDate dob) { this.dob = dob; }
        public Long getProgrammeId() { return programmeId; }
        public void setProgrammeId(Long programmeId) { this.programmeId = programmeId; }
        public Integer getAdmissionYear() { return admissionYear; }
        public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getResolutionNote() { return resolutionNote; }
        public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
    }

    public static class UploadBatchDto {
        private Long id;
        private Long institutionId;
        private String institutionName;
        private String filename;
        private Integer totalRows;
        private Integer validRows;
        private Integer errorRows;
        private Integer duplicateRows;
        private String uploadedBy;
        private LocalDateTime uploadedAt;
        private String status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getInstitutionId() { return institutionId; }
        public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }
        public String getInstitutionName() { return institutionName; }
        public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public Integer getTotalRows() { return totalRows; }
        public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }
        public Integer getValidRows() { return validRows; }
        public void setValidRows(Integer validRows) { this.validRows = validRows; }
        public Integer getErrorRows() { return errorRows; }
        public void setErrorRows(Integer errorRows) { this.errorRows = errorRows; }
        public Integer getDuplicateRows() { return duplicateRows; }
        public void setDuplicateRows(Integer duplicateRows) { this.duplicateRows = duplicateRows; }
        public String getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class UploadResultDto {
        private Long batchId;
        private String filename;
        private int totalRows;
        private int validRows;
        private int errorRows;
        private int duplicateRows;
        private String status;
        private List<ValidationIssueDto> issues;

        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public int getTotalRows() { return totalRows; }
        public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
        public int getValidRows() { return validRows; }
        public void setValidRows(int validRows) { this.validRows = validRows; }
        public int getErrorRows() { return errorRows; }
        public void setErrorRows(int errorRows) { this.errorRows = errorRows; }
        public int getDuplicateRows() { return duplicateRows; }
        public void setDuplicateRows(int duplicateRows) { this.duplicateRows = duplicateRows; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<ValidationIssueDto> getIssues() { return issues; }
        public void setIssues(List<ValidationIssueDto> issues) { this.issues = issues; }
    }

    public static class RegulatorKpiDto {
        private long totalInstitutions;
        private long approvedIntake;
        private long actualEnrolment;
        private long supernumerarySeats;
        private long openExceptions;
        private long intakeExceededProgrammes;

        public long getTotalInstitutions() { return totalInstitutions; }
        public void setTotalInstitutions(long totalInstitutions) { this.totalInstitutions = totalInstitutions; }
        public long getApprovedIntake() { return approvedIntake; }
        public void setApprovedIntake(long approvedIntake) { this.approvedIntake = approvedIntake; }
        public long getActualEnrolment() { return actualEnrolment; }
        public void setActualEnrolment(long actualEnrolment) { this.actualEnrolment = actualEnrolment; }
        public long getSupernumerarySeats() { return supernumerarySeats; }
        public void setSupernumerarySeats(long supernumerarySeats) { this.supernumerarySeats = supernumerarySeats; }
        public long getOpenExceptions() { return openExceptions; }
        public void setOpenExceptions(long openExceptions) { this.openExceptions = openExceptions; }
        public long getIntakeExceededProgrammes() { return intakeExceededProgrammes; }
        public void setIntakeExceededProgrammes(long intakeExceededProgrammes) { this.intakeExceededProgrammes = intakeExceededProgrammes; }
    }

    public static class ProgrammeIntakeDto {
        private Long programmeId;
        private String programmeCode;
        private String programmeName;
        private Long institutionId;
        private String institutionCode;
        private String institutionName;
        private int approvedIntake;
        private int supernumeraryQuota;
        private int totalPermitted;
        private long actualEnrolment;
        private double utilizationPercent;
        private String status; // OK (<90%), WARNING (90-100%), EXCEEDED (>100%)

        public Long getProgrammeId() { return programmeId; }
        public void setProgrammeId(Long programmeId) { this.programmeId = programmeId; }
        public String getProgrammeCode() { return programmeCode; }
        public void setProgrammeCode(String programmeCode) { this.programmeCode = programmeCode; }
        public String getProgrammeName() { return programmeName; }
        public void setProgrammeName(String programmeName) { this.programmeName = programmeName; }
        public Long getInstitutionId() { return institutionId; }
        public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }
        public String getInstitutionCode() { return institutionCode; }
        public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }
        public String getInstitutionName() { return institutionName; }
        public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
        public int getApprovedIntake() { return approvedIntake; }
        public void setApprovedIntake(int approvedIntake) { this.approvedIntake = approvedIntake; }
        public int getSupernumeraryQuota() { return supernumeraryQuota; }
        public void setSupernumeraryQuota(int supernumeraryQuota) { this.supernumeraryQuota = supernumeraryQuota; }
        public int getTotalPermitted() { return totalPermitted; }
        public void setTotalPermitted(int totalPermitted) { this.totalPermitted = totalPermitted; }
        public long getActualEnrolment() { return actualEnrolment; }
        public void setActualEnrolment(long actualEnrolment) { this.actualEnrolment = actualEnrolment; }
        public double getUtilizationPercent() { return utilizationPercent; }
        public void setUtilizationPercent(double utilizationPercent) { this.utilizationPercent = utilizationPercent; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class CategoryBreakdownDto {
        private String category;
        private long count;
        private double percentage;

        public CategoryBreakdownDto(String category, long count, double percentage) {
            this.category = category;
            this.count = count;
            this.percentage = percentage;
        }

        public String getCategory() { return category; }
        public long getCount() { return count; }
        public double getPercentage() { return percentage; }
    }

    public static class YearTrendDto {
        private int year;
        private long count;

        public YearTrendDto(int year, long count) {
            this.year = year;
            this.count = count;
        }

        public int getYear() { return year; }
        public long getCount() { return count; }
    }

    public static class AnomalyDto {
        private Long id;
        private String type;
        private String title;
        private String description;
        private String institutionCode;
        private String institutionName;
        private String programmeName;
        private String severity;
        private LocalDateTime createdAt;
        private String status;
        private Long studentId;
        private String apaarId;
        private String maskedApaarId;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getInstitutionCode() { return institutionCode; }
        public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }
        public String getInstitutionName() { return institutionName; }
        public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
        public String getProgrammeName() { return programmeName; }
        public void setProgrammeName(String programmeName) { this.programmeName = programmeName; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getApaarId() { return apaarId; }
        public void setApaarId(String apaarId) { this.apaarId = apaarId; }
        public String getMaskedApaarId() { return maskedApaarId; }
        public void setMaskedApaarId(String maskedApaarId) { this.maskedApaarId = maskedApaarId; }
    }

    public static class AnomalyActionRequest {
        private String action; // REVIEW, DISMISS, ESCALATE
        private String note;

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class CreateTokenRequest {
        private Long studentId;
        private String allowedFields; // e.g. "NAME,DOB,INSTITUTION,PROGRAMME,ADMISSION_YEAR,STATUS"
        private int expiryHours = 72;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getAllowedFields() { return allowedFields; }
        public void setAllowedFields(String allowedFields) { this.allowedFields = allowedFields; }
        public int getExpiryHours() { return expiryHours; }
        public void setExpiryHours(int expiryHours) { this.expiryHours = expiryHours; }
    }

    public static class TokenResponse {
        private String token;
        private String verificationUrl;
        private LocalDateTime expiresAt;
        private String allowedFields;

        public TokenResponse(String token, String verificationUrl, LocalDateTime expiresAt, String allowedFields) {
            this.token = token;
            this.verificationUrl = verificationUrl;
            this.expiresAt = expiresAt;
            this.allowedFields = allowedFields;
        }

        public String getToken() { return token; }
        public String getVerificationUrl() { return verificationUrl; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public String getAllowedFields() { return allowedFields; }
    }

    public static class PublicVerificationDto {
        private boolean valid;
        private String error;
        private String token;
        private String studentName;
        private String maskedApaarId;
        private LocalDate dob;
        private String gender;
        private String institutionCode;
        private String institutionName;
        private String programmeName;
        private Integer admissionYear;
        private String verificationStatus;
        private LocalDateTime verifiedAt;
        private List<String> permittedFields;
        private String issuer = "AICTE APAAR-Connect Verification Gateway";

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getMaskedApaarId() { return maskedApaarId; }
        public void setMaskedApaarId(String maskedApaarId) { this.maskedApaarId = maskedApaarId; }
        public LocalDate getDob() { return dob; }
        public void setDob(LocalDate dob) { this.dob = dob; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getInstitutionCode() { return institutionCode; }
        public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }
        public String getInstitutionName() { return institutionName; }
        public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
        public String getProgrammeName() { return programmeName; }
        public void setProgrammeName(String programmeName) { this.programmeName = programmeName; }
        public Integer getAdmissionYear() { return admissionYear; }
        public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }
        public String getVerificationStatus() { return verificationStatus; }
        public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
        public LocalDateTime getVerifiedAt() { return verifiedAt; }
        public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
        public List<String> getPermittedFields() { return permittedFields; }
        public void setPermittedFields(List<String> permittedFields) { this.permittedFields = permittedFields; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
    }

    public static class AuditLogDto {
        private Long id;
        private String actor;
        private String role;
        private String action;
        private String entity;
        private String entityId;
        private String ip;
        private LocalDateTime timestamp;
        private String details;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getActor() { return actor; }
        public void setActor(String actor) { this.actor = actor; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getEntity() { return entity; }
        public void setEntity(String entity) { this.entity = entity; }
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }
}
