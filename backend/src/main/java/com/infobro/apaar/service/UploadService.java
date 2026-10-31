package com.infobro.apaar.service;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import com.infobro.apaar.validation.ValidationEngine;
import com.infobro.apaar.validation.ValidationIssueInfo;
import com.infobro.apaar.validation.ValidationResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    private final InstitutionRepository institutionRepository;
    private final ProgrammeRepository programmeRepository;
    private final StudentRepository studentRepository;
    private final UploadBatchRepository uploadBatchRepository;
    private final ValidationIssueRepository validationIssueRepository;
    private final ValidationEngine validationEngine;
    private final AuditService auditService;
    private final InstitutionAccessService access;

    public UploadService(InstitutionRepository institutionRepository,
                         ProgrammeRepository programmeRepository,
                         StudentRepository studentRepository,
                         UploadBatchRepository uploadBatchRepository,
                         ValidationIssueRepository validationIssueRepository,
                         ValidationEngine validationEngine,
                         AuditService auditService, InstitutionAccessService access) {
        this.institutionRepository = institutionRepository;
        this.programmeRepository = programmeRepository;
        this.studentRepository = studentRepository;
        this.uploadBatchRepository = uploadBatchRepository;
        this.validationIssueRepository = validationIssueRepository;
        this.validationEngine = validationEngine;
        this.auditService = auditService;
        this.access = access;
    }

    @Transactional(rollbackFor = Exception.class)
    public DTOs.UploadResultDto processCsvUpload(Long institutionId, MultipartFile file, String actor, String role, String ip) throws Exception {
        institutionId = access.requireInstitution(institutionId);
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null
            || !file.getOriginalFilename().toLowerCase(java.util.Locale.ROOT).endsWith(".csv"))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "A non-empty CSV file is required.");
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new NoSuchElementException("Institution not found."));

        List<Programme> programmes = programmeRepository.findByInstitutionId(institutionId);
        Map<String, Programme> programmeMap = new HashMap<>();
        for (Programme p : programmes) {
            programmeMap.put(p.getCode().trim().toUpperCase(), p);
            programmeMap.put(p.getName().trim().toUpperCase(), p);
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.csv";

        UploadBatch batch = new UploadBatch(
                institution,
                originalFilename,
                0, 0, 0, 0,
                actor,
                "PROCESSING"
        );
        batch = uploadBatchRepository.save(batch);

        int totalRows = 0;
        int validRows = 0;
        int errorRows = 0;
        int duplicateRows = 0;

        Set<String> batchApaars = new HashSet<>();
        List<DTOs.ValidationIssueDto> issueDtos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build())) {

            for (CSVRecord record : csvParser) {
                totalRows++;
                String apaarId = getRecordValue(record, "apaar_id", "apaar", "apaarid");
                String name = getRecordValue(record, "name", "student_name", "studentname");
                String dobStr = getRecordValue(record, "dob", "date_of_birth", "birthdate");
                String gender = getRecordValue(record, "gender", "sex");
                String progKey = getRecordValue(record, "programme", "programme_code", "program", "course");
                String yearStr = getRecordValue(record, "admission_year", "year", "batch_year");
                String category = getRecordValue(record, "category", "admission_category");
                String email = getRecordValue(record, "email", "email_id");
                String phone = getRecordValue(record, "phone", "mobile", "contact");

                LocalDate dob = parseDate(dobStr);
                Integer year = parseInteger(yearStr);
                if (year == null) year = 2026;

                Programme prog = null;
                if (progKey != null) {
                    prog = programmeMap.get(progKey.trim().toUpperCase());
                }
                if (prog == null && !programmes.isEmpty()) {
                    prog = programmes.get(0);
                }

                Student student = new Student(
                        apaarId != null ? validationEngine.normalizeApaarId(apaarId) : null,
                        name != null ? name.trim() : "",
                        dob != null ? dob : LocalDate.of(2005, 1, 1),
                        gender != null ? gender : "Other",
                        prog,
                        institution,
                        year,
                        category != null ? category.trim().toUpperCase().replace(" ", "_") : "REGULAR",
                        email,
                        phone,
                        "PENDING",
                        "true".equalsIgnoreCase(getRecordValue(record, "consent_given"))
                );

                ValidationResult valResult = validationEngine.validateStudent(student, batchApaars, true);
                student.setVerificationStatus(valResult.getStatus());

                Student savedStudent = studentRepository.save(student);

                if ("VALID".equals(valResult.getStatus())) {
                    validRows++;
                } else if ("DUPLICATE".equals(valResult.getStatus())) {
                    duplicateRows++;
                } else {
                    errorRows++;
                }

                // Persist issues
                for (ValidationIssueInfo issueInfo : valResult.getIssues()) {
                    ValidationIssue issue = new ValidationIssue(
                            savedStudent,
                            batch,
                            issueInfo.getRuleCode(),
                            issueInfo.getSeverity(),
                            issueInfo.getMessage(),
                            issueInfo.getSuggestedFix(),
                            "CSV Row " + totalRows + ": " + record.toString()
                    );
                    ValidationIssue savedIssue = validationIssueRepository.save(issue);

                    DTOs.ValidationIssueDto dto = new DTOs.ValidationIssueDto();
                    dto.setId(savedIssue.getId());
                    dto.setStudentId(savedStudent.getId());
                    dto.setStudentName(savedStudent.getName());
                    dto.setApaarId(savedStudent.getApaarId());
                    dto.setMaskedApaarId(savedStudent.getMaskedApaarId());
                    dto.setBatchId(batch.getId());
                    dto.setRuleCode(savedIssue.getRuleCode());
                    dto.setSeverity(savedIssue.getSeverity());
                    dto.setMessage(savedIssue.getMessage());
                    dto.setSuggestedFix(savedIssue.getSuggestedFix());
                    dto.setStatus(savedIssue.getStatus());
                    dto.setCreatedAt(savedIssue.getCreatedAt());
                    issueDtos.add(dto);
                }
            }

            batch.setTotalRows(totalRows);
            batch.setValidRows(validRows);
            batch.setErrorRows(errorRows);
            batch.setDuplicateRows(duplicateRows);
            batch.setStatus("COMPLETED");
            uploadBatchRepository.save(batch);

            auditService.log(
                    actor,
                    role,
                    "UPLOAD_BATCH",
                    "UploadBatch",
                    batch.getId().toString(),
                    ip,
                    String.format("Uploaded '%s': Total=%d, Valid=%d, Errors=%d, Duplicates=%d",
                            originalFilename, totalRows, validRows, errorRows, duplicateRows)
            );

        } catch (Exception ex) {
            log.error("Error processing CSV upload", ex);
            batch.setStatus("FAILED");
            uploadBatchRepository.save(batch);
            throw ex;
        }

        DTOs.UploadResultDto result = new DTOs.UploadResultDto();
        result.setBatchId(batch.getId());
        result.setFilename(batch.getFilename());
        result.setTotalRows(totalRows);
        result.setValidRows(validRows);
        result.setErrorRows(errorRows);
        result.setDuplicateRows(duplicateRows);
        result.setStatus("COMPLETED");
        result.setIssues(issueDtos);

        return result;
    }

    public Page<DTOs.UploadBatchDto> getBatchesByInstitution(Long institutionId, int page, int size) {
        institutionId = access.requireInstitution(institutionId);
        return uploadBatchRepository.findByInstitutionIdOrderByUploadedAtDesc(institutionId, PageRequest.of(page, size))
                .map(this::toBatchDto);
    }

    private DTOs.UploadBatchDto toBatchDto(UploadBatch b) {
        DTOs.UploadBatchDto dto = new DTOs.UploadBatchDto();
        dto.setId(b.getId());
        dto.setInstitutionId(b.getInstitution().getId());
        dto.setInstitutionName(b.getInstitution().getName());
        dto.setFilename(b.getFilename());
        dto.setTotalRows(b.getTotalRows());
        dto.setValidRows(b.getValidRows());
        dto.setErrorRows(b.getErrorRows());
        dto.setDuplicateRows(b.getDuplicateRows());
        dto.setUploadedBy(b.getUploadedBy());
        dto.setUploadedAt(b.getUploadedAt());
        dto.setStatus(b.getStatus());
        return dto;
    }

    private String getRecordValue(CSVRecord record, String... possibleHeaders) {
        for (String h : possibleHeaders) {
            if (record.isMapped(h)) {
                String val = record.get(h);
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            }
        }
        return null;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        dateStr = dateStr.trim();
        List<String> patterns = Arrays.asList("yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "MM/dd/yyyy");
        for (String p : patterns) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(p));
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Integer parseInteger(String str) {
        if (str == null) return null;
        try {
            return Integer.parseInt(str.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
