package com.infobro.apaar.controller;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "Immutable audit log query, filters, and regulatory export")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/logs")
    @Operation(summary = "Get audit logs with actor, role, action, and date filters")
    public ResponseEntity<Page<DTOs.AuditLogDto>> getAuditLogs(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(auditService.getAuditLogs(actor, role, action, entity, fromTime, toTime, page, size));
    }

    @GetMapping("/export")
    @Operation(summary = "Export audit trail as CSV")
    public ResponseEntity<byte[]> exportAuditCsv(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entity
    ) {
        String csv = auditService.exportAuditLogsCsv(actor, role, action, entity);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit_trail_export.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
