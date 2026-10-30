package com.infobro.apaar.controller;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.UserAccountRepository;
import com.infobro.apaar.service.IntakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/regulator")
@Tag(name = "Regulator", description = "AICTE Regulation Bureau dashboard, grouped intake comparisons, anomaly center, and alerts")
public class RegulatorController {

    private final IntakeService intakeService;
    private final UserAccountRepository userAccountRepository;

    public RegulatorController(IntakeService intakeService, UserAccountRepository userAccountRepository) {
        this.intakeService = intakeService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/kpis")
    @Operation(summary = "Get national-level intake and enrolment KPIs for AICTE Regulator")
    public ResponseEntity<DTOs.RegulatorKpiDto> getKpis() {
        return ResponseEntity.ok(intakeService.getRegulatorKpis());
    }

    @GetMapping("/programmes-intake")
    @Operation(summary = "Approved vs Actual Enrolment breakdown by programme with utilization status (OK, Warning, Exceeded)")
    public ResponseEntity<List<DTOs.ProgrammeIntakeDto>> getProgrammeIntakes(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Integer academicYear
    ) {
        return ResponseEntity.ok(intakeService.getProgrammeIntakes(institutionId, academicYear));
    }

    @GetMapping("/category-breakdown")
    @Operation(summary = "Category distribution (Regular, Supernumerary, NRI, OCI, Foreign National)")
    public ResponseEntity<List<DTOs.CategoryBreakdownDto>> getCategoryBreakdown() {
        return ResponseEntity.ok(intakeService.getCategoryBreakdown());
    }

    @GetMapping("/year-trends")
    @Operation(summary = "Multi-year enrolment trend data")
    public ResponseEntity<List<DTOs.YearTrendDto>> getYearTrends() {
        return ResponseEntity.ok(intakeService.getYearTrends());
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Anomaly & Duplicate Center list: cross-institution duplicates, sudden spikes, contact clusters")
    public ResponseEntity<List<DTOs.AnomalyDto>> getAnomalies() {
        return ResponseEntity.ok(intakeService.getAnomalies());
    }

    @PostMapping("/anomalies/{id}/action")
    @Operation(summary = "Perform Review, Dismiss, or Escalate action on an anomaly with regulatory notes")
    public ResponseEntity<Map<String, String>> handleAnomalyAction(
            @PathVariable Long id,
            @RequestBody DTOs.AnomalyActionRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElseThrow();
        String clientIp = httpRequest.getRemoteAddr();

        intakeService.handleAnomalyAction(id, request, user.getEmail(), user.getRole(), clientIp);

        Map<String, String> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("message", "Action '" + request.getAction() + "' applied to anomaly ID " + id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Real-time alert stream of intake exceedances and duplicate anomalies")
    public ResponseEntity<List<Map<String, Object>>> getAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();

        // Generate current alerts based on live data
        List<DTOs.ProgrammeIntakeDto> progs = intakeService.getProgrammeIntakes(null, null);
        for (DTOs.ProgrammeIntakeDto p : progs) {
            if ("EXCEEDED".equals(p.getStatus())) {
                Map<String, Object> a = new HashMap<>();
                a.put("id", "alert-exceed-" + p.getProgrammeId());
                a.put("type", "EXCEEDANCE");
                a.put("level", "CRITICAL");
                a.put("title", "Intake Exceeded: " + p.getInstitutionCode() + " - " + p.getProgrammeCode());
                a.put("message", "Actual enrolment (" + p.getActualEnrolment() + ") exceeds approved + supernumerary quota (" + p.getTotalPermitted() + ")");
                a.put("timestamp", LocalDateTime.now().minusMinutes(5));
                alerts.add(a);
            }
        }

        List<DTOs.AnomalyDto> anomalies = intakeService.getAnomalies();
        for (DTOs.AnomalyDto anomaly : anomalies) {
            if ("DUPLICATE_CROSS_INSTITUTION".equals(anomaly.getType())) {
                Map<String, Object> a = new HashMap<>();
                a.put("id", "alert-anomaly-" + anomaly.getId());
                a.put("type", "DUPLICATE");
                a.put("level", "WARNING");
                a.put("title", "Cross-Institution Duplicate Detected: APAAR " + anomaly.getMaskedApaarId());
                a.put("message", anomaly.getDescription());
                a.put("timestamp", anomaly.getCreatedAt());
                alerts.add(a);
            }
        }

        return ResponseEntity.ok(alerts);
    }
}
