package com.infobro.apaar.service;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.Student;
import com.infobro.apaar.entity.VerificationRequest;
import com.infobro.apaar.entity.VerificationToken;
import com.infobro.apaar.repository.StudentRepository;
import com.infobro.apaar.repository.VerificationRequestRepository;
import com.infobro.apaar.repository.VerificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class VerificationService {

    @Value("${apaar.verify-token.ttl-minutes:4320}")
    private long defaultTokenTtlMinutes;

    private final VerificationTokenRepository tokenRepository;
    private final VerificationRequestRepository requestRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;
    private final InstitutionAccessService access;

    public VerificationService(VerificationTokenRepository tokenRepository,
                               VerificationRequestRepository requestRepository,
                               StudentRepository studentRepository,
                               AuditService auditService, InstitutionAccessService access) {
        this.tokenRepository = tokenRepository;
        this.requestRepository = requestRepository;
        this.studentRepository = studentRepository;
        this.auditService = auditService;
        this.access = access;
    }

    @Transactional
    public DTOs.TokenResponse createVerificationToken(DTOs.CreateTokenRequest request,
                                                      String actor, String role, String ip) {
        access.requireStudent(request.getStudentId());
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new NoSuchElementException("Student not found with ID: " + request.getStudentId()));

        if (!Boolean.TRUE.equals(student.getConsentGiven())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                "Student has not granted consent for credential verification sharing.");
        }

        String rawToken = UUID.randomUUID().toString().replace("-", "") +
                Long.toHexString(System.currentTimeMillis());

        String allowedFields = (request.getAllowedFields() != null && !request.getAllowedFields().trim().isEmpty())
                ? request.getAllowedFields()
                : "NAME,APAAR,INSTITUTION,PROGRAMME,ADMISSION_YEAR,STATUS";

        LocalDateTime expiresAt = request.getExpiryHours() > 0
                ? LocalDateTime.now().plusHours(request.getExpiryHours())
                : LocalDateTime.now().plusMinutes(defaultTokenTtlMinutes);

        VerificationToken token = new VerificationToken(
                student,
                rawToken,
                expiresAt,
                allowedFields,
                actor
        );
        tokenRepository.save(token);

        auditService.log(
                actor,
                role,
                "GENERATE_VERIFICATION_TOKEN",
                "VerificationToken",
                rawToken,
                ip,
                "Generated secure verification token for student APAAR " + student.getMaskedApaarId()
        );

        String verifyUrl = "/verify/" + rawToken;
        return new DTOs.TokenResponse(rawToken, verifyUrl, expiresAt, allowedFields);
    }

    @Transactional
    public DTOs.PublicVerificationDto verifyToken(String tokenStr, String clientIp, String userAgent) {
        DTOs.PublicVerificationDto dto = new DTOs.PublicVerificationDto();
        dto.setToken(tokenStr);
        dto.setVerifiedAt(LocalDateTime.now());

        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(tokenStr);
        if (tokenOpt.isEmpty()) {
            dto.setValid(false);
            dto.setError("Invalid verification token. No record found for this token.");
            return dto;
        }

        VerificationToken token = tokenOpt.get();
        if (token.isExpired()) {
            dto.setValid(false);
            dto.setError("Verification token has expired on " + token.getExpiresAt());

            VerificationRequest req = new VerificationRequest(token, clientIp, userAgent, "EXPIRED");
            requestRepository.save(req);
            return dto;
        }

        if (!Boolean.TRUE.equals(token.getActive())) {
            dto.setValid(false);
            dto.setError("Verification token has been revoked.");
            return dto;
        }

        Student student = token.getStudent();
        if (!Boolean.TRUE.equals(student.getConsentGiven())) {
            dto.setValid(false);
            dto.setError("Consent for credential verification has been withdrawn.");
            return dto;
        }
        dto.setValid(true);
        dto.setIssuer("AICTE APAAR GARUDA NETRA Verification Gateway");

        // Parse allowed fields to strictly filter data minimization
        Set<String> allowed = new HashSet<>();
        if (token.getAllowedFields() != null) {
            for (String f : token.getAllowedFields().split(",")) {
                allowed.add(f.trim().toUpperCase());
            }
        }
        dto.setPermittedFields(new ArrayList<>(allowed));

        if (allowed.contains("NAME")) {
            dto.setStudentName(student.getName());
        }
        if (allowed.contains("APAAR")) {
            dto.setMaskedApaarId(student.getMaskedApaarId());
        }
        if (allowed.contains("DOB")) {
            dto.setDob(student.getDob());
        }
        if (allowed.contains("GENDER")) {
            dto.setGender(student.getGender());
        }
        if (allowed.contains("INSTITUTION")) {
            dto.setInstitutionCode(student.getInstitution().getCode());
            dto.setInstitutionName(student.getInstitution().getName());
        }
        if (allowed.contains("PROGRAMME")) {
            dto.setProgrammeName(student.getProgramme().getName());
        }
        if (allowed.contains("ADMISSION_YEAR")) {
            dto.setAdmissionYear(student.getAdmissionYear());
        }
        if (allowed.contains("STATUS")) {
            dto.setVerificationStatus(student.getVerificationStatus());
        }

        // Record verification request
        VerificationRequest req = new VerificationRequest(token, clientIp, userAgent, "SUCCESS");
        requestRepository.save(req);

        auditService.log(
                "PUBLIC_VERIFIER",
                "ROLE_AUTHORIZED_VERIFIER",
                "VERIFY_RECORD",
                "Student",
                student.getId().toString(),
                clientIp,
                "Verified student record via token " + tokenStr.substring(0, Math.min(tokenStr.length(), 8)) + "..."
        );

        return dto;
    }
}
