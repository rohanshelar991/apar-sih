package com.infobro.apaar.controller;

import com.infobro.apaar.service.StudentPortalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student-portal")
public class StudentPortalController {
    private final StudentPortalService service;
    public StudentPortalController(StudentPortalService service) { this.service = service; }

    @GetMapping("/profile") public Map<String, Object> profile(Authentication auth) { return service.profile(auth.getName()); }
    @GetMapping("/profile-updates") public List<Map<String, Object>> updates(Authentication auth) { return service.profileUpdateHistory(auth.getName()); }
    @PostMapping("/profile-updates") public Map<String, Object> request(@Valid @RequestBody ProfileUpdate body, Authentication auth, HttpServletRequest request) {
        return service.requestProfileUpdate(auth.getName(), body.changes(), request.getRemoteAddr());
    }
    public record ProfileUpdate(@NotEmpty Map<String, Object> changes) {}
}
