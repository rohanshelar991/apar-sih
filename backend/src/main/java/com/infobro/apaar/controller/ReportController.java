package com.infobro.apaar.controller;

import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.UserAccountRepository;
import com.infobro.apaar.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Regulatory summary report generation and downloads (PDF and CSV)")
public class ReportController {

    private final ReportService reportService;
    private final UserAccountRepository userAccountRepository;

    public ReportController(ReportService reportService, UserAccountRepository userAccountRepository) {
        this.reportService = reportService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/summary/csv")
    @Operation(summary = "Generate and download AICTE Regulatory Summary Report in CSV format")
    public ResponseEntity<byte[]> downloadSummaryCsv(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Integer academicYear,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElseThrow();
        String clientIp = httpRequest.getRemoteAddr();

        String csvContent = reportService.generateSummaryCsv(
                institutionId, academicYear, user.getEmail(), user.getRole(), clientIp
        );
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aicte_regulatory_summary_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/summary/pdf")
    @Operation(summary = "Generate and download official AICTE Regulatory Summary Report in PDF format")
    public ResponseEntity<byte[]> downloadSummaryPdf(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Integer academicYear,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) throws Exception {
        UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElseThrow();
        String clientIp = httpRequest.getRemoteAddr();

        byte[] pdfBytes = reportService.generateSummaryPdf(
                institutionId, academicYear, user.getEmail(), user.getRole(), clientIp
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"aicte_regulatory_summary_report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
