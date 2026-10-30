package com.infobro.apaar.controller;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.UserAccountRepository;
import com.infobro.apaar.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validation")
@Tag(name = "Validation", description = "Validation issues, inline error correction, and rule resolution workflows")
public class ValidationController {

    private final ValidationService validationService;
    private final UserAccountRepository userAccountRepository;

    public ValidationController(ValidationService validationService, UserAccountRepository userAccountRepository) {
        this.validationService = validationService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/issues")
    @Operation(summary = "List validation issues with status, severity, and ruleCode filters")
    public ResponseEntity<Page<DTOs.ValidationIssueDto>> getIssues(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String ruleCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Authentication authentication
    ) {
        if (institutionId == null && authentication != null) {
            UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null && "ROLE_INSTITUTION_ADMIN".equals(user.getRole()) && user.getInstitution() != null) {
                institutionId = user.getInstitution().getId();
            }
        }

        Page<DTOs.ValidationIssueDto> issues = validationService.getIssues(institutionId, status, severity, ruleCode, page, size);
        return ResponseEntity.ok(issues);
    }

    @PostMapping("/issues/{id}/resolve")
    @Operation(summary = "Inline correction workflow: update flagged record, re-validate, and mark issue resolved")
    public ResponseEntity<DTOs.ValidationIssueDto> resolveIssue(
            @PathVariable Long id,
            @RequestBody DTOs.IssueResolveRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElseThrow();
        String clientIp = httpRequest.getRemoteAddr();

        DTOs.ValidationIssueDto resolved = validationService.resolveIssue(
                id, request, user.getEmail(), user.getRole(), clientIp
        );
        return ResponseEntity.ok(resolved);
    }
}
