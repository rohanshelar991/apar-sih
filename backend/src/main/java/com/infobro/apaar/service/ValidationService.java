package com.infobro.apaar.service;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.Programme;
import com.infobro.apaar.entity.Student;
import com.infobro.apaar.entity.StudentHistory;
import com.infobro.apaar.entity.ValidationIssue;
import com.infobro.apaar.repository.ProgrammeRepository;
import com.infobro.apaar.repository.StudentHistoryRepository;
import com.infobro.apaar.repository.StudentRepository;
import com.infobro.apaar.repository.ValidationIssueRepository;
import com.infobro.apaar.validation.ValidationEngine;
import com.infobro.apaar.validation.ValidationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class ValidationService {

    private final ValidationIssueRepository validationIssueRepository;
    private final StudentRepository studentRepository;
    private final ProgrammeRepository programmeRepository;
    private final StudentHistoryRepository studentHistoryRepository;
    private final ValidationEngine validationEngine;
    private final AuditService auditService;
    private final InstitutionAccessService access;

    public ValidationService(ValidationIssueRepository validationIssueRepository,
                             StudentRepository studentRepository,
                             ProgrammeRepository programmeRepository,
                             StudentHistoryRepository studentHistoryRepository,
                             ValidationEngine validationEngine,
                             AuditService auditService, InstitutionAccessService access) {
        this.validationIssueRepository = validationIssueRepository;
        this.studentRepository = studentRepository;
        this.programmeRepository = programmeRepository;
        this.studentHistoryRepository = studentHistoryRepository;
        this.validationEngine = validationEngine;
        this.auditService = auditService;
        this.access = access;
    }

    public Page<DTOs.ValidationIssueDto> getIssues(Long institutionId, String status, String severity,
                                                   String ruleCode, int page, int size) {
        institutionId = access.scopeInstitution(institutionId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return validationIssueRepository.findWithFilters(institutionId, status, severity, ruleCode, pageable)
                .map(this::toDto);
    }

    @Transactional
    public DTOs.ValidationIssueDto resolveIssue(Long issueId, DTOs.IssueResolveRequest request,
                                               String actor, String role, String ip) {
        access.requireIssue(issueId);
        ValidationIssue issue = validationIssueRepository.findById(issueId)
                .orElseThrow(() -> new NoSuchElementException("Issue not found with ID: " + issueId));

        Student student = issue.getStudent();
        if (student != null) {
            if (request.getProgrammeId() != null) access.requireProgramme(request.getProgrammeId(), student.getInstitution().getId());
            // Apply requested updates
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                recordHistory(student, "name", student.getName(), request.getName().trim(), actor);
                student.setName(request.getName().trim());
            }
            if (request.getApaarId() != null && !request.getApaarId().trim().isEmpty()) {
                String norm = validationEngine.normalizeApaarId(request.getApaarId());
                recordHistory(student, "apaarId", student.getApaarId(), norm, actor);
                student.setApaarId(norm);
            }
            if (request.getDob() != null) {
                recordHistory(student, "dob", String.valueOf(student.getDob()), String.valueOf(request.getDob()), actor);
                student.setDob(request.getDob());
            }
            if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
                recordHistory(student, "category", student.getCategory(), request.getCategory().trim().toUpperCase(), actor);
                student.setCategory(request.getCategory().trim().toUpperCase());
            }
            if (request.getAdmissionYear() != null) {
                recordHistory(student, "admissionYear", String.valueOf(student.getAdmissionYear()),
                        String.valueOf(request.getAdmissionYear()), actor);
                student.setAdmissionYear(request.getAdmissionYear());
            }
            if (request.getProgrammeId() != null && !request.getProgrammeId().equals(student.getProgramme().getId())) {
                Programme prog = programmeRepository.findById(request.getProgrammeId())
                        .orElseThrow(() -> new NoSuchElementException("Programme not found with ID: " + request.getProgrammeId()));
                recordHistory(student, "programme", student.getProgramme().getName(), prog.getName(), actor);
                student.setProgramme(prog);
            }

            // Re-validate student
            ValidationResult valResult = validationEngine.validateStudent(student, new HashSet<>(), false);
            student.setVerificationStatus(valResult.getStatus());
            studentRepository.save(student);
        }

        // Mark issue as resolved
        issue.setStatus("RESOLVED");
        issue.setResolvedBy(actor);
        issue.setResolvedAt(LocalDateTime.now());
        if (request.getResolutionNote() != null) {
            issue.setMessage(issue.getMessage() + " [Resolution Note: " + request.getResolutionNote() + "]");
        }
        ValidationIssue savedIssue = validationIssueRepository.save(issue);

        auditService.log(
                actor,
                role,
                "RESOLVE_ISSUE",
                "ValidationIssue",
                issueId.toString(),
                ip,
                String.format("Resolved issue %s for student %s. Note: %s",
                        issue.getRuleCode(), student != null ? student.getMaskedApaarId() : "N/A", request.getResolutionNote())
        );

        return toDto(savedIssue);
    }

    private void recordHistory(Student student, String field, String oldVal, String newVal, String actor) {
        if (!Objects.equals(oldVal, newVal)) {
            StudentHistory history = new StudentHistory(student, field, oldVal, newVal, actor);
            studentHistoryRepository.save(history);
        }
    }

    private DTOs.ValidationIssueDto toDto(ValidationIssue issue) {
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
