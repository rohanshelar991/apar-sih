package com.infobro.apaar.service;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.Institution;
import com.infobro.apaar.entity.Programme;
import com.infobro.apaar.entity.Student;
import com.infobro.apaar.entity.ValidationIssue;
import com.infobro.apaar.repository.InstitutionRepository;
import com.infobro.apaar.repository.ProgrammeRepository;
import com.infobro.apaar.repository.StudentRepository;
import com.infobro.apaar.repository.ValidationIssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class IntakeService {

    private final InstitutionRepository institutionRepository;
    private final ProgrammeRepository programmeRepository;
    private final StudentRepository studentRepository;
    private final ValidationIssueRepository validationIssueRepository;
    private final AuditService auditService;

    public IntakeService(InstitutionRepository institutionRepository,
                         ProgrammeRepository programmeRepository,
                         StudentRepository studentRepository,
                         ValidationIssueRepository validationIssueRepository,
                         AuditService auditService) {
        this.institutionRepository = institutionRepository;
        this.programmeRepository = programmeRepository;
        this.studentRepository = studentRepository;
        this.validationIssueRepository = validationIssueRepository;
        this.auditService = auditService;
    }

    public DTOs.RegulatorKpiDto getRegulatorKpis() {
        DTOs.RegulatorKpiDto kpi = new DTOs.RegulatorKpiDto();
        kpi.setTotalInstitutions(institutionRepository.count());

        List<Programme> programmes = programmeRepository.findAll();
        long totalApproved = 0;
        long totalSupernumerary = 0;
        for (Programme p : programmes) {
            totalApproved += p.getApprovedIntake() != null ? p.getApprovedIntake() : 0;
            totalSupernumerary += p.getSupernumeraryQuota() != null ? p.getSupernumeraryQuota() : 0;
        }
        kpi.setApprovedIntake(totalApproved);
        kpi.setSupernumerarySeats(totalSupernumerary);

        long actualEnrolled = studentRepository.count();
        kpi.setActualEnrolment(actualEnrolled);

        long openIssues = validationIssueRepository.countByStatus("OPEN");
        kpi.setOpenExceptions(openIssues);

        // Count how many programmes are exceeded
        long exceededCount = 0;
        for (Programme p : programmes) {
            long count = studentRepository.countByProgrammeId(p.getId());
            if (count > p.getTotalPermittedIntake()) {
                exceededCount++;
            }
        }
        kpi.setIntakeExceededProgrammes(exceededCount);

        return kpi;
    }

    public List<DTOs.ProgrammeIntakeDto> getProgrammeIntakes(Long institutionId, Integer academicYear) {
        List<Programme> programmes;
        if (institutionId != null) {
            programmes = programmeRepository.findByInstitutionId(institutionId);
        } else if (academicYear != null) {
            programmes = programmeRepository.findByAcademicYear(academicYear);
        } else {
            programmes = programmeRepository.findAll();
        }

        List<DTOs.ProgrammeIntakeDto> result = new ArrayList<>();
        for (Programme p : programmes) {
            DTOs.ProgrammeIntakeDto dto = new DTOs.ProgrammeIntakeDto();
            dto.setProgrammeId(p.getId());
            dto.setProgrammeCode(p.getCode());
            dto.setProgrammeName(p.getName());
            dto.setInstitutionId(p.getInstitution().getId());
            dto.setInstitutionCode(p.getInstitution().getCode());
            dto.setInstitutionName(p.getInstitution().getName());
            dto.setApprovedIntake(p.getApprovedIntake());
            dto.setSupernumeraryQuota(p.getSupernumeraryQuota());
            dto.setTotalPermitted(p.getTotalPermittedIntake());

            long actual = studentRepository.countByProgrammeId(p.getId());
            dto.setActualEnrolment(actual);

            double utilization = 0.0;
            if (p.getTotalPermittedIntake() > 0) {
                utilization = Math.round((double) actual / p.getTotalPermittedIntake() * 1000.0) / 10.0;
            }
            dto.setUtilizationPercent(utilization);

            if (utilization > 100.0) {
                dto.setStatus("EXCEEDED");
            } else if (utilization >= 90.0) {
                dto.setStatus("WARNING");
            } else {
                dto.setStatus("OK");
            }

            result.add(dto);
        }
        return result;
    }

    public List<DTOs.CategoryBreakdownDto> getCategoryBreakdown() {
        List<Object[]> rows = studentRepository.countGroupedByCategory();
        long total = studentRepository.count();
        List<DTOs.CategoryBreakdownDto> list = new ArrayList<>();

        for (Object[] r : rows) {
            String category = (String) r[0];
            long count = ((Number) r[1]).longValue();
            double pct = total > 0 ? Math.round((double) count / total * 1000.0) / 10.0 : 0.0;
            list.add(new DTOs.CategoryBreakdownDto(category, count, pct));
        }
        return list;
    }

    public List<DTOs.YearTrendDto> getYearTrends() {
        List<Object[]> rows = studentRepository.countGroupedByYear();
        List<DTOs.YearTrendDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            Integer year = (Integer) r[0];
            long count = ((Number) r[1]).longValue();
            list.add(new DTOs.YearTrendDto(year != null ? year : 2026, count));
        }
        return list;
    }

    public List<DTOs.AnomalyDto> getAnomalies() {
        List<DTOs.AnomalyDto> anomalies = new ArrayList<>();

        // 1. Cross-institution duplicate records
        List<ValidationIssue> issues = validationIssueRepository.findOpenAnomalies();
        for (ValidationIssue vi : issues) {
            DTOs.AnomalyDto dto = new DTOs.AnomalyDto();
            dto.setId(vi.getId());
            dto.setType(vi.getRuleCode());
            dto.setTitle(formatAnomalyTitle(vi.getRuleCode()));
            dto.setDescription(vi.getMessage());
            dto.setSeverity(vi.getSeverity());
            dto.setCreatedAt(vi.getCreatedAt());
            dto.setStatus(vi.getStatus());
            if (vi.getStudent() != null) {
                dto.setStudentId(vi.getStudent().getId());
                dto.setApaarId(vi.getStudent().getApaarId());
                dto.setMaskedApaarId(vi.getStudent().getMaskedApaarId());
                dto.setInstitutionCode(vi.getStudent().getInstitution().getCode());
                dto.setInstitutionName(vi.getStudent().getInstitution().getName());
                dto.setProgrammeName(vi.getStudent().getProgramme().getName());
            }
            anomalies.add(dto);
        }

        // 2. Programmes with exceeded intake
        List<Programme> programmes = programmeRepository.findAll();
        for (Programme p : programmes) {
            long count = studentRepository.countByProgrammeId(p.getId());
            if (count > p.getTotalPermittedIntake()) {
                DTOs.AnomalyDto dto = new DTOs.AnomalyDto();
                dto.setId(900000L + p.getId());
                dto.setType("INTAKE_CAPACITY_EXCEEDED");
                dto.setTitle("Intake Exceeded: " + p.getInstitution().getCode() + " - " + p.getName());
                dto.setDescription(String.format("Programme exceeded total permitted intake (%d). Actual enrolments: %d (Utilization: %.1f%%)",
                        p.getTotalPermittedIntake(), count, ((double) count / p.getTotalPermittedIntake() * 100)));
                dto.setSeverity("HIGH");
                dto.setCreatedAt(LocalDateTime.now().minusHours(2));
                dto.setStatus("OPEN");
                dto.setInstitutionCode(p.getInstitution().getCode());
                dto.setInstitutionName(p.getInstitution().getName());
                dto.setProgrammeName(p.getName());
                anomalies.add(dto);
            }
        }

        // 3. Clustered contact details anomaly (many records sharing same phone/email)
        List<Object[]> dupEmails = studentRepository.findDuplicateEmails();
        for (Object[] r : dupEmails) {
            String email = (String) r[0];
            long count = ((Number) r[1]).longValue();
            if (count >= 3) {
                DTOs.AnomalyDto dto = new DTOs.AnomalyDto();
                dto.setId(800000L + Math.abs(email.hashCode() % 10000));
                dto.setType("SHARED_CONTACT_CLUSTER");
                dto.setTitle("Suspicious Shared Contact: Email '" + email + "'");
                dto.setDescription("Multiple student admission records (" + count + " students) registered with identical email address.");
                dto.setSeverity("MEDIUM");
                dto.setCreatedAt(LocalDateTime.now().minusHours(5));
                dto.setStatus("OPEN");
                dto.setInstitutionCode("MULTIPLE");
                dto.setInstitutionName("Cross-Institutional Pattern");
                dto.setProgrammeName("Cluster");
                anomalies.add(dto);
            }
        }

        return anomalies;
    }

    @Transactional
    public void handleAnomalyAction(Long id, DTOs.AnomalyActionRequest request, String actor, String role, String ip) {
        if (id < 800000L) {
            // It's a real ValidationIssue
            ValidationIssue issue = validationIssueRepository.findById(id).orElse(null);
            if (issue != null) {
                String action = request.getAction() != null ? request.getAction().toUpperCase() : "REVIEW";
                if ("DISMISS".equals(action)) {
                    issue.setStatus("DISMISSED");
                } else if ("ESCALATE".equals(action)) {
                    issue.setStatus("ESCALATED");
                } else {
                    issue.setStatus("UNDER_REVIEW");
                }
                issue.setResolvedBy(actor);
                issue.setResolvedAt(LocalDateTime.now());
                if (request.getNote() != null) {
                    issue.setMessage(issue.getMessage() + " [Regulator Action " + action + ": " + request.getNote() + "]");
                }
                validationIssueRepository.save(issue);
            }
        }

        auditService.log(
                actor,
                role,
                "ANOMALY_" + (request.getAction() != null ? request.getAction().toUpperCase() : "ACTION"),
                "Anomaly",
                String.valueOf(id),
                ip,
                "Regulator action performed on anomaly ID " + id + ". Note: " + request.getNote()
        );
    }

    private String formatAnomalyTitle(String ruleCode) {
        switch (ruleCode) {
            case "DUPLICATE_CROSS_INSTITUTION":
                return "Cross-Institution Duplicate Enrolment";
            case "DUPLICATE_SAME_INSTITUTION":
                return "Duplicate Enrolment in Same Programme";
            case "INCONSISTENT_DATA":
                return "Conflicting Demographic / Identity Data";
            case "INTAKE_EXCEEDED":
                return "Programme Intake Threshold Exceeded";
            default:
                return "Data Integrity Exception (" + ruleCode + ")";
        }
    }
}
