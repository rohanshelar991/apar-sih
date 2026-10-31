package com.infobro.apaar.controller;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.UserAccountRepository;
import com.infobro.apaar.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student admission record management, single entry, directory search, and CSV export")
public class StudentController {

    private final StudentService studentService;
    private final UserAccountRepository userAccountRepository;

    public StudentController(StudentService studentService, UserAccountRepository userAccountRepository) {
        this.studentService = studentService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    @Operation(summary = "Search and filter student records with pagination")
    public ResponseEntity<Page<DTOs.StudentDto>> getStudents(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long programmeId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        boolean maskApaar = false;
        if (authentication != null) {
            UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null && "ROLE_AUTHORIZED_VERIFIER".equals(user.getRole())) {
                maskApaar = true;
            }
        }

        Page<DTOs.StudentDto> result = studentService.getStudents(
                institutionId, programmeId, category, status, year, search,
                page, size, sortBy, direction, maskApaar
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student record by ID")
    public ResponseEntity<DTOs.StudentDto> getStudentById(@PathVariable Long id, Authentication authentication) {
        boolean maskApaar = false;
        if (authentication != null) {
            UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElse(null);
            if (user != null && "ROLE_AUTHORIZED_VERIFIER".equals(user.getRole())) {
                maskApaar = true;
            }
        }
        return ResponseEntity.ok(studentService.getStudentById(id, maskApaar));
    }

    @PostMapping
    @Operation(summary = "Create single student record with real-time validation")
    public ResponseEntity<DTOs.StudentDto> createStudent(
            @RequestBody DTOs.StudentFormRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElseThrow();
        Long instId = user.getInstitution() != null ? user.getInstitution().getId() : null;
        String clientIp = httpRequest.getRemoteAddr();

        DTOs.StudentDto created = studentService.createStudent(
                instId, request, user.getEmail(), user.getRole(), clientIp
        );
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student record and re-evaluate validation rules")
    public ResponseEntity<DTOs.StudentDto> updateStudent(
            @PathVariable Long id,
            @RequestBody DTOs.StudentFormRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        UserAccount user = userAccountRepository.findByEmail(authentication.getName()).orElseThrow();
        String clientIp = httpRequest.getRemoteAddr();

        DTOs.StudentDto updated = studentService.updateStudent(
                id, request, user.getEmail(), user.getRole(), clientIp
        );
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/export")
    @Operation(summary = "Export filtered student records as CSV")
    public ResponseEntity<byte[]> exportStudentsCsv(
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Long programmeId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year,
            Authentication authentication
    ) {
        String csvData = studentService.exportStudentsCsv(institutionId, programmeId, category, status, year);
        byte[] bytes = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students_export.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
