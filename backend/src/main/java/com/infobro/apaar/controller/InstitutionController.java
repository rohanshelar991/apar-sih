package com.infobro.apaar.controller;

import com.infobro.apaar.entity.Institution;
import com.infobro.apaar.entity.Programme;
import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.InstitutionRepository;
import com.infobro.apaar.repository.ProgrammeRepository;
import com.infobro.apaar.repository.StudentRepository;
import com.infobro.apaar.repository.UserAccountRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/institution")
@Tag(name = "Institution", description = "Institution profile, programme lists, and institutional metrics")
public class InstitutionController {

    private final InstitutionRepository institutionRepository;
    private final ProgrammeRepository programmeRepository;
    private final StudentRepository studentRepository;
    private final UserAccountRepository userAccountRepository;
    private final com.infobro.apaar.service.InstitutionAccessService access;

    public InstitutionController(InstitutionRepository institutionRepository,
                                 ProgrammeRepository programmeRepository,
                                 StudentRepository studentRepository,
                                 UserAccountRepository userAccountRepository, com.infobro.apaar.service.InstitutionAccessService access) {
        this.institutionRepository = institutionRepository;
        this.programmeRepository = programmeRepository;
        this.studentRepository = studentRepository;
        this.userAccountRepository = userAccountRepository;
        this.access = access;
    }

    @GetMapping("/current")
    @Operation(summary = "Get logged in institution administrator's institution profile")
    public ResponseEntity<Institution> getCurrentInstitution(Authentication authentication) {
        UserAccount user = access.currentUser();
        Long own = access.scopeInstitution(null);
        if (own == null) return ResponseEntity.ok(institutionRepository.findAll().stream().findFirst()
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Institution not found.")));
        return ResponseEntity.ok(user.getInstitution());
    }

    @GetMapping("/all")
    @Operation(summary = "List all AICTE accredited institutions")
    public ResponseEntity<List<Institution>> getAllInstitutions() {
        Long own = access.scopeInstitution(null);
        return ResponseEntity.ok(own == null ? institutionRepository.findAll() : institutionRepository.findById(own).stream().toList());
    }

    @GetMapping("/programmes")
    @Operation(summary = "List programmes for institution")
    public ResponseEntity<List<Programme>> getProgrammes(@RequestParam(required = false) Long institutionId,
                                                         Authentication authentication) {
        institutionId = access.scopeInstitution(institutionId);
        if (institutionId != null) {
            return ResponseEntity.ok(programmeRepository.findByInstitutionId(institutionId));
        }
        return ResponseEntity.ok(programmeRepository.findAll());
    }

    @GetMapping("/stats")
    @Operation(summary = "Get overview KPIs for current institution")
    public ResponseEntity<Map<String, Object>> getInstitutionStats(@RequestParam(required = false) Long institutionId,
                                                                   Authentication authentication) {
        institutionId = access.scopeInstitution(institutionId);
        if (institutionId == null) institutionId = institutionRepository.findAll().stream().findFirst()
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Institution not found.")).getId();

        long total = studentRepository.countByInstitutionId(institutionId);
        long valid = studentRepository.countByInstitutionIdAndVerificationStatus(institutionId, "VALID");
        long pending = studentRepository.countByInstitutionIdAndVerificationStatus(institutionId, "PENDING");
        long duplicates = studentRepository.countByInstitutionIdAndVerificationStatus(institutionId, "DUPLICATE");
        long errors = studentRepository.countByInstitutionIdAndVerificationStatus(institutionId, "ERROR");

        List<Programme> progs = programmeRepository.findByInstitutionId(institutionId);
        long approvedTotal = 0;
        long superTotal = 0;
        for (Programme p : progs) {
            approvedTotal += p.getApprovedIntake() != null ? p.getApprovedIntake() : 0;
            superTotal += p.getSupernumeraryQuota() != null ? p.getSupernumeraryQuota() : 0;
        }

        double utilPct = (approvedTotal + superTotal) > 0 ?
                Math.round((double) total / (approvedTotal + superTotal) * 1000.0) / 10.0 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", total);
        stats.put("validStudents", valid);
        stats.put("pendingStudents", pending);
        stats.put("duplicateStudents", duplicates);
        stats.put("errorStudents", errors);
        stats.put("approvedIntake", approvedTotal);
        stats.put("supernumeraryQuota", superTotal);
        stats.put("totalPermittedIntake", approvedTotal + superTotal);
        stats.put("utilizationPercent", utilPct);

        return ResponseEntity.ok(stats);
    }
}
