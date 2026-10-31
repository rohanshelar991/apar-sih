package com.infobro.apaar.controller;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.UserAccountRepository;
import com.infobro.apaar.service.StudentService;
import com.infobro.apaar.service.InstitutionAccessService;
import com.infobro.apaar.service.AuditService;
import com.infobro.apaar.repository.StudentRepository;
import com.infobro.apaar.entity.Student;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student admission record management, single entry, directory search, and CSV export")
public class StudentController {

    private final StudentService studentService;
    private final UserAccountRepository userAccountRepository;
    private final StudentRepository studentRepository;
    private final InstitutionAccessService accessService;
    private final AuditService auditService;

    public StudentController(StudentService studentService, UserAccountRepository userAccountRepository,
                             StudentRepository studentRepository, InstitutionAccessService accessService, AuditService auditService) {
        this.studentService = studentService;
        this.userAccountRepository = userAccountRepository;
        this.studentRepository = studentRepository;
        this.accessService = accessService;
        this.auditService = auditService;
    }

    @PutMapping("/{id}/account-link")
    @Transactional
    public ResponseEntity<Void> linkStudentAccount(@PathVariable Long id, @Valid @RequestBody AccountLink body,
                                                   Authentication authentication, HttpServletRequest request) {
        accessService.requireStudent(id);
        Student student = studentRepository.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Student not found."));
        String email = body.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (student.getEmail() == null || !student.getEmail().trim().equalsIgnoreCase(email))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Account email must match the institution record email.");
        UserAccount account = userAccountRepository.findByEmail(email).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Student account not found."));
        if (!Boolean.TRUE.equals(account.getActive()) || !"ROLE_STUDENT".equals(account.getRole()) || account.getStudent() != null)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "This account cannot be linked to a student record.");
        if (userAccountRepository.findByStudentId(id).isPresent())
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "A student portal account is already linked.");
        account.setStudent(student);
        userAccountRepository.save(account);
        UserAccount actor = userAccountRepository.findByEmail(authentication.getName()).orElseThrow();
        auditService.log(actor.getEmail(), actor.getRole(), "STUDENT_ACCOUNT_LINKED", "Student", id.toString(), request.getRemoteAddr(), "Linked a student portal account after matching the institution email.");
        return ResponseEntity.noContent().build();
    }

    public record AccountLink(@NotBlank @Email @Size(max = 120) String email) {}

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
