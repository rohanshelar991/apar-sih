package com.infobro.apaar.service;

import com.infobro.apaar.entity.StudentProfileUpdateRequest;
import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.StudentProfileUpdateRequestRepository;
import com.infobro.apaar.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class StudentPortalService {
    private static final Set<String> PROFILE_FIELDS = Set.of("email", "phone", "gender");
    private final UserAccountRepository users;
    private final StudentProfileUpdateRequestRepository requests;
    private final AuditService audit;

    public StudentPortalService(UserAccountRepository users, StudentProfileUpdateRequestRepository requests, AuditService audit) {
        this.users = users; this.requests = requests; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> profile(String email) {
        UserAccount account = studentAccount(email);
        if (account.getStudent() == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Your account is awaiting institution record linking.");
        var student = account.getStudent();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", student.getId()); data.put("name", student.getName()); data.put("email", student.getEmail());
        data.put("phone", student.getPhone()); data.put("gender", student.getGender()); data.put("maskedApaarId", student.getMaskedApaarId());
        data.put("verificationStatus", student.getVerificationStatus()); data.put("admissionYear", student.getAdmissionYear());
        data.put("category", student.getCategory()); data.put("programme", student.getProgramme().getName());
        data.put("institution", student.getInstitution().getName()); data.put("institutionCode", student.getInstitution().getCode());
        return data;
    }

    @Transactional
    public Map<String, Object> requestProfileUpdate(String email, Map<String, Object> changes, String ip) {
        UserAccount account = studentAccount(email);
        if (account.getStudent() == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Your account is awaiting institution record linking.");
        if (changes == null || changes.isEmpty() || !PROFILE_FIELDS.containsAll(changes.keySet()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only email, phone and gender may be requested for update.");
        Map<String, Object> normalizedChanges = new LinkedHashMap<>();
        for (var entry : changes.entrySet()) {
            if (!(entry.getValue() instanceof String value) || value.isBlank() || value.length() > (entry.getKey().equals("email") ? 120 : 20))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile values must be non-empty strings within the allowed length.");
            normalizedChanges.put(entry.getKey(), value.trim());
        }
        Long studentId = account.getStudent().getId();
        if (requests.existsByStudentIdAndStatus(studentId, "PENDING"))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A profile update request is already pending.");
        StudentProfileUpdateRequest request = new StudentProfileUpdateRequest();
        request.setStudent(account.getStudent()); request.setRequesterEmail(email); request.setRequestedChanges(normalizedChanges);
        request = requests.save(request);
        audit.log(email, account.getRole(), "STUDENT_PROFILE_UPDATE_REQUESTED", "StudentProfileUpdateRequest", request.getId().toString(), ip,
                "Student submitted a profile update request.");
        return Map.of("id", request.getId(), "status", request.getStatus(), "createdAt", request.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> profileUpdateHistory(String email) {
        UserAccount account = studentAccount(email);
        if (account.getStudent() == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Your account is awaiting institution record linking.");
        return requests.findByStudentIdOrderByCreatedAtDesc(account.getStudent().getId()).stream().map(r -> {
            Map<String, Object> dto = new LinkedHashMap<>(); dto.put("id", r.getId()); dto.put("changes", r.getRequestedChanges());
            dto.put("status", r.getStatus()); dto.put("remarks", r.getRemarks()); dto.put("createdAt", r.getCreatedAt()); dto.put("reviewedAt", r.getReviewedAt()); return dto;
        }).toList();
    }

    private UserAccount studentAccount(String email) {
        UserAccount account = users.findByEmail(email).filter(u -> Boolean.TRUE.equals(u.getActive()) && "ROLE_STUDENT".equals(u.getRole()))
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Access denied."));
        return account;
    }
}
