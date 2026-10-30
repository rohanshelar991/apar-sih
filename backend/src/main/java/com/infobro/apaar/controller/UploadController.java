package com.infobro.apaar.controller;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.UserAccountRepository;
import com.infobro.apaar.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "Upload", description = "Bulk CSV admission data upload, async validation engine, and batch tracking")
public class UploadController {

    private final UploadService uploadService;
    private final UserAccountRepository userAccountRepository;

    public UploadController(UploadService uploadService, UserAccountRepository userAccountRepository) {
        this.uploadService = uploadService;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "Upload student admissions CSV file and run comprehensive validation pipeline")
    public ResponseEntity<DTOs.UploadResultDto> uploadCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "institutionId", required = false) Long institutionId,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) throws Exception {
        UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElseThrow();
        if (institutionId == null) {
            institutionId = (user.getInstitution() != null) ? user.getInstitution().getId() : 1L;
        }
        String clientIp = httpRequest.getRemoteAddr();

        DTOs.UploadResultDto result = uploadService.processCsvUpload(
                institutionId, file, user.getEmail(), user.getRole(), clientIp
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/batches")
    @Operation(summary = "Get historical upload batches for an institution")
    public ResponseEntity<Page<DTOs.UploadBatchDto>> getBatches(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        if (institutionId == null && authentication != null) {
            UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null && user.getInstitution() != null) {
                institutionId = user.getInstitution().getId();
            }
        }
        if (institutionId == null) institutionId = 1L;

        return ResponseEntity.ok(uploadService.getBatchesByInstitution(institutionId, page, size));
    }
}
