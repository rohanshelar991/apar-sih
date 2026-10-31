package com.infobro.apaar.service;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import com.infobro.apaar.validation.ValidationEngine;
import com.infobro.apaar.validation.ValidationIssueInfo;
import com.infobro.apaar.validation.ValidationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final ProgrammeRepository programmeRepository;
    private final ValidationIssueRepository validationIssueRepository;
    private final StudentHistoryRepository studentHistoryRepository;
    private final ValidationEngine validationEngine;
    private final AuditService auditService;
    private final InstitutionAccessService access;
    private final VerificationTokenRepository tokenRepository;

    public StudentService(StudentRepository studentRepository,
                          InstitutionRepository institutionRepository,
                          ProgrammeRepository programmeRepository,
                          ValidationIssueRepository validationIssueRepository,
                          StudentHistoryRepository studentHistoryRepository,
                          ValidationEngine validationEngine,
                          AuditService auditService, InstitutionAccessService access, VerificationTokenRepository tokenRepository) {
        this.studentRepository = studentRepository;
        this.institutionRepository = institutionRepository;
        this.programmeRepository = programmeRepository;
        this.validationIssueRepository = validationIssueRepository;
        this.studentHistoryRepository = studentHistoryRepository;
        this.validationEngine = validationEngine;
        this.auditService = auditService;
        this.access = access;
        this.tokenRepository = tokenRepository;
    }

    public Page<DTOs.StudentDto> getStudents(Long institutionId, Long programmeId, String category,
                                             String status, Integer year, String search,
                                             int page, int size, String sortBy, String direction,
                                             boolean maskApaar) {
        institutionId = access.scopeInstitution(institutionId);
        if (programmeId != null) {
            var programme = programmeRepository.findById(programmeId).orElseThrow();
            access.requireProgramme(programmeId, institutionId != null ? institutionId : programme.getInstitution().getId());
        }
        Sort sort = Sort.by("asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy != null ? sortBy : "id");
        Pageable pageable = PageRequest.of(page, size, sort);

        return studentRepository.findWithFilters(institutionId, programmeId, category, status, year, search, pageable)
                .map(s -> toDto(s, maskApaar));
    }

    public DTOs.StudentDto getStudentById(Long id, boolean maskApaar) {
        access.requireStudent(id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Student not found with ID: " + id));
        return toDto(student, maskApaar);
    }

    @Transactional
    public DTOs.StudentDto createStudent(Long institutionId, DTOs.StudentFormRequest request,
                                         String actor, String role, String ip) {
        institutionId = access.scopeInstitution(institutionId);
        if (institutionId == null) institutionId = programmeRepository.findById(request.getProgrammeId()).orElseThrow().getInstitution().getId();
        access.requireProgramme(request.getProgrammeId(), institutionId);
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new NoSuchElementException("Institution not found."));

        Programme programme = programmeRepository.findById(request.getProgrammeId())
                .orElseThrow(() -> new NoSuchElementException("Programme not found with ID: " + request.getProgrammeId()));

        Student student = new Student(
                validationEngine.normalizeApaarId(request.getApaarId()),
                request.getName().trim(),
                request.getDob(),
                request.getGender() != null ? request.getGender() : "Other",
                programme,
                institution,
                request.getAdmissionYear() != null ? request.getAdmissionYear() : programme.getAcademicYear(),
                request.getCategory() != null ? request.getCategory() : "REGULAR",
                request.getEmail(),
                request.getPhone(),
                "PENDING",
                Boolean.TRUE.equals(request.getConsentGiven())
        );

        ValidationResult valResult = validationEngine.validateStudent(student, new HashSet<>(), false);
        student.setVerificationStatus(valResult.getStatus());
        Student saved = studentRepository.save(student);

        // Save issues if any
        for (ValidationIssueInfo issueInfo : valResult.getIssues()) {
            ValidationIssue issue = new ValidationIssue(
                    saved,
                    null,
                    issueInfo.getRuleCode(),
                    issueInfo.getSeverity(),
                    issueInfo.getMessage(),
                    issueInfo.getSuggestedFix(),
                    "Manual Single Entry: " + student.getName()
            );
            validationIssueRepository.save(issue);
        }

        auditService.log(
                actor,
                role,
                "CREATE_STUDENT",
                "Student",
                saved.getId().toString(),
                ip,
                "Created student record for APAAR " + saved.getMaskedApaarId() + ", status: " + saved.getVerificationStatus()
        );

        return toDto(saved, false);
    }

    @Transactional
    public DTOs.StudentDto updateStudent(Long studentId, DTOs.StudentFormRequest request,
                                         String actor, String role, String ip) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found with ID: " + studentId));

        access.requireStudent(studentId);
        if (request.getProgrammeId() != null) access.requireProgramme(request.getProgrammeId(), student.getInstitution().getId());
        if (request.getConsentGiven() != null) {
            recordHistoryIfChanged(student, "consentGiven", String.valueOf(student.getConsentGiven()), String.valueOf(request.getConsentGiven()), actor);
            student.setConsentGiven(request.getConsentGiven());
            if (!request.getConsentGiven()) {
                for (var token : tokenRepository.findByStudentIdOrderByCreatedAtDesc(studentId)) {
                    token.setActive(false);
                    tokenRepository.save(token);
                }
            }
        }

        // Track history
        if (request.getName() != null) recordHistoryIfChanged(student, "name", student.getName(), request.getName(), actor);
        if (request.getApaarId() != null) recordHistoryIfChanged(student, "apaarId", student.getApaarId(), validationEngine.normalizeApaarId(request.getApaarId()), actor);
        if (request.getDob() != null) recordHistoryIfChanged(student, "dob", student.getDob() != null ? student.getDob().toString() : null,
                request.getDob() != null ? request.getDob().toString() : null, actor);
        if (request.getCategory() != null) recordHistoryIfChanged(student, "category", student.getCategory(), request.getCategory(), actor);
        if (request.getAdmissionYear() != null) recordHistoryIfChanged(student, "admissionYear", String.valueOf(student.getAdmissionYear()),
                String.valueOf(request.getAdmissionYear()), actor);

        if (request.getName() != null) student.setName(request.getName().trim());
        if (request.getApaarId() != null) student.setApaarId(validationEngine.normalizeApaarId(request.getApaarId()));
        if (request.getDob() != null) student.setDob(request.getDob());
        if (request.getGender() != null) student.setGender(request.getGender());
        if (request.getCategory() != null) student.setCategory(request.getCategory());
        if (request.getEmail() != null) student.setEmail(request.getEmail());
        if (request.getPhone() != null) student.setPhone(request.getPhone());
        if (request.getAdmissionYear() != null) student.setAdmissionYear(request.getAdmissionYear());

        if (request.getProgrammeId() != null && !request.getProgrammeId().equals(student.getProgramme().getId())) {
            Programme newProg = programmeRepository.findById(request.getProgrammeId())
                    .orElseThrow(() -> new NoSuchElementException("Programme not found with ID: " + request.getProgrammeId()));
            recordHistoryIfChanged(student, "programme", student.getProgramme().getName(), newProg.getName(), actor);
            student.setProgramme(newProg);
        }

        // Re-validate
        ValidationResult valResult = validationEngine.validateStudent(student, new HashSet<>(), false);
        student.setVerificationStatus(valResult.getStatus());

        // Resolve previous open issues if now valid
        List<ValidationIssue> existingIssues = validationIssueRepository.findByStudentId(student.getId());
        if (valResult.isValid()) {
            for (ValidationIssue issue : existingIssues) {
                if ("OPEN".equals(issue.getStatus())) {
                    issue.setStatus("RESOLVED");
                    issue.setResolvedBy(actor);
                    issue.setResolvedAt(LocalDateTime.now());
                    validationIssueRepository.save(issue);
                }
            }
        } else {
            // Update open issues
            for (ValidationIssueInfo issueInfo : valResult.getIssues()) {
                boolean alreadyOpen = existingIssues.stream()
                        .anyMatch(i -> "OPEN".equals(i.getStatus()) && i.getRuleCode().equals(issueInfo.getRuleCode()));
                if (!alreadyOpen) {
                    ValidationIssue newIssue = new ValidationIssue(
                            student,
                            null,
                            issueInfo.getRuleCode(),
                            issueInfo.getSeverity(),
                            issueInfo.getMessage(),
                            issueInfo.getSuggestedFix(),
                            "Updated record correction: " + student.getName()
                    );
                    validationIssueRepository.save(newIssue);
                }
            }
        }

        Student updated = studentRepository.save(student);

        auditService.log(
                actor,
                role,
                "UPDATE_STUDENT",
                "Student",
                updated.getId().toString(),
                ip,
                "Updated student record APAAR " + updated.getMaskedApaarId() + ", new status: " + updated.getVerificationStatus()
        );

        return toDto(updated, false);
    }

    private void recordHistoryIfChanged(Student student, String field, String oldVal, String newVal, String actor) {
        if (!Objects.equals(oldVal, newVal)) {
            StudentHistory history = new StudentHistory(student, field, oldVal, newVal, actor);
            studentHistoryRepository.save(history);
        }
    }

    public String exportStudentsCsv(Long institutionId, Long programmeId, String category, String status, Integer year) {
        institutionId = access.scopeInstitution(institutionId);
        if (programmeId != null) {
            var programme = programmeRepository.findById(programmeId).orElseThrow();
            access.requireProgramme(programmeId, institutionId != null ? institutionId : programme.getInstitution().getId());
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Student ID,APAAR ID,Name,DOB,Gender,Programme,Institution,Admission Year,Category,Email,Phone,Status");

        List<Student> students = studentRepository.findAllWithFilters(institutionId, programmeId, category, status, year);
        for (Student s : students) {
            pw.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%d\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    s.getId(),
                    escapeCsv(s.getMaskedApaarId()),
                    escapeCsv(s.getName()),
                    s.getDob(),
                    escapeCsv(s.getGender()),
                    escapeCsv(s.getProgramme().getName()),
                    escapeCsv(s.getInstitution().getName()),
                    s.getAdmissionYear(),
                    escapeCsv(s.getCategory()),
                    escapeCsv(s.getEmail()),
                    escapeCsv(s.getPhone()),
                    escapeCsv(s.getVerificationStatus())
            );
        }
        return sw.toString();
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }

    public DTOs.StudentDto toDto(Student student, boolean maskApaar) {
        DTOs.StudentDto dto = new DTOs.StudentDto();
        dto.setId(student.getId());
        dto.setApaarId(maskApaar ? student.getMaskedApaarId() : student.getApaarId());
        dto.setMaskedApaarId(student.getMaskedApaarId());
        dto.setName(student.getName());
        dto.setDob(student.getDob());
        dto.setGender(student.getGender());
        dto.setProgrammeId(student.getProgramme().getId());
        dto.setProgrammeName(student.getProgramme().getName());
        dto.setProgrammeCode(student.getProgramme().getCode());
        dto.setInstitutionId(student.getInstitution().getId());
        dto.setInstitutionCode(student.getInstitution().getCode());
        dto.setInstitutionName(student.getInstitution().getName());
        dto.setAdmissionYear(student.getAdmissionYear());
        dto.setCategory(student.getCategory());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setVerificationStatus(student.getVerificationStatus());
        dto.setConsentGiven(student.getConsentGiven());
        dto.setCreatedAt(student.getCreatedAt());
        dto.setUpdatedAt(student.getUpdatedAt());

        List<ValidationIssue> issues = validationIssueRepository.findByStudentId(student.getId());
        if (!issues.isEmpty()) {
            dto.setIssues(issues.stream().map(this::toIssueDto).collect(Collectors.toList()));
        } else {
            dto.setIssues(Collections.emptyList());
        }
        return dto;
    }

    public DTOs.ValidationIssueDto toIssueDto(ValidationIssue issue) {
        DTOs.ValidationIssueDto dto = new DTOs.ValidationIssueDto();
        dto.setId(issue.getId());
        if (issue.getStudent() != null) {
            dto.setStudentId(issue.getStudent().getId());
            dto.setStudentName(issue.getStudent().getName());
            dto.setApaarId(issue.getStudent().getApaarId());
            dto.setMaskedApaarId(issue.getStudent().getMaskedApaarId());
        }
        if (issue.getUploadBatch() != null) {
            dto.setBatchId(issue.getUploadBatch().getId());
        }
        dto.setRuleCode(issue.getRuleCode());
        dto.setSeverity(issue.getSeverity());
        dto.setMessage(issue.getMessage());
        dto.setSuggestedFix(issue.getSuggestedFix());
        dto.setStatus(issue.getStatus());
        dto.setResolvedBy(issue.getResolvedBy());
        dto.setResolvedAt(issue.getResolvedAt());
        dto.setCreatedAt(issue.getCreatedAt());
        return dto;
    }
}
