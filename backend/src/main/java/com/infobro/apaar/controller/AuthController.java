package com.infobro.apaar.controller;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.UserAccountRepository;
import com.infobro.apaar.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, demo account selection, and profile endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserAccountRepository userAccountRepository;

    public AuthController(AuthService authService, UserAccountRepository userAccountRepository) {
        this.authService = authService;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT bearer token")
    public ResponseEntity<DTOs.LoginResponse> login(@RequestBody DTOs.LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        DTOs.LoginResponse response = authService.login(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/student")
    public ResponseEntity<Map<String, String>> registerStudent(@Valid @RequestBody StudentRegistration body, HttpServletRequest request) {
        authService.registerStudent(body.email(), body.fullName(), body.password(), request.getRemoteAddr());
        return ResponseEntity.status(201).body(Map.of("status", "registered", "message", "Account created. An institution administrator must link your academic record before portal access."));
    }

    public record StudentRegistration(@NotBlank @Email @Size(max = 120) String email,
                                      @NotBlank @Size(max = 120) String fullName,
                                      @NotBlank @Size(min = 12, max = 128) String password) {}

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).build();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("fullName", user.getFullName());
        map.put("role", user.getRole());
        if (user.getStudent() != null) map.put("studentId", user.getStudent().getId());
        if (user.getInstitution() != null) {
            map.put("institutionId", user.getInstitution().getId());
            map.put("institutionCode", user.getInstitution().getCode());
            map.put("institutionName", user.getInstitution().getName());
        }
        return ResponseEntity.ok(map);
    }

    @GetMapping("/demo-accounts")
    @Operation(summary = "List seeded one-click demo login accounts for hackathon judges")
    public ResponseEntity<List<Map<String, String>>> getDemoAccounts() {
        List<Map<String, String>> accounts = new ArrayList<>();

        Map<String, String> inst = new HashMap<>();
        inst.put("role", "ROLE_INSTITUTION_ADMIN");
        inst.put("title", "DEMO Institution Administrator");
        inst.put("institution", "Demo Engineering Institute (DEMO001)");
        inst.put("email", "admin@example.com");
        inst.put("password", "");
        inst.put("description", "Uploads student batch CSVs, reviews validation errors, corrects flagged records, exports directories.");
        accounts.add(inst);

        Map<String, String> reg = new HashMap<>();
        reg.put("role", "ROLE_AICTE_REGULATOR");
        reg.put("title", "DEMO National Regulator");
        reg.put("institution", "Isolated demo environment");
        reg.put("email", "regulator@example.com");
        reg.put("password", "");
        reg.put("description", "Cross-institutional oversight, intake compliance charts, anomaly center, duplicate escalations, PDF reports.");
        accounts.add(reg);

        Map<String, String> ver = new HashMap<>();
        ver.put("role", "ROLE_AUTHORIZED_VERIFIER");
        ver.put("title", "DEMO Authorized Verifier");
        ver.put("institution", "Isolated demo environment");
        ver.put("email", "verifier@example.com");
        ver.put("password", "");
        ver.put("description", "Verifies candidate authenticity via QR code or verification token with permitted fields data minimization.");
        accounts.add(ver);

        return ResponseEntity.ok(accounts);
    }
}
