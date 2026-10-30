package com.infobro.apaar.controller;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.UserAccountRepository;
import com.infobro.apaar.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Verification", description = "Public-style secure token verification and student credential QR generation")
public class VerificationController {

    private final VerificationService verificationService;
    private final UserAccountRepository userAccountRepository;

    public VerificationController(VerificationService verificationService, UserAccountRepository userAccountRepository) {
        this.verificationService = verificationService;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping("/api/tokens/generate")
    @Operation(summary = "Generate secure verification token and QR payload for a consented student record")
    public ResponseEntity<DTOs.TokenResponse> generateToken(
            @RequestBody DTOs.CreateTokenRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElseThrow();
        String clientIp = httpRequest.getRemoteAddr();

        DTOs.TokenResponse response = verificationService.createVerificationToken(
                request, user.getEmail(), user.getRole(), clientIp
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/verify/{token}")
    @Operation(summary = "Public verification endpoint: verify student credentials using token, returning only permitted fields")
    public ResponseEntity<DTOs.PublicVerificationDto> verifyToken(
            @PathVariable String token,
            HttpServletRequest request
    ) {
        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        DTOs.PublicVerificationDto result = verificationService.verifyToken(token, clientIp, userAgent);
        return ResponseEntity.ok(result);
    }
}
