package com.infobro.apaar.service;

import com.infobro.apaar.config.JwtTokenProvider;
import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.UserAccount;
import com.infobro.apaar.repository.UserAccountRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;

    public AuthService(UserAccountRepository userAccountRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
    }

    public DTOs.LoginResponse login(DTOs.LoginRequest request, String clientIp) {
        UserAccount user = userAccountRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadCredentialsException("User account is disabled");
        }

        Long institutionId = user.getInstitution() != null ? user.getInstitution().getId() : null;
        String institutionCode = user.getInstitution() != null ? user.getInstitution().getCode() : null;
        String institutionName = user.getInstitution() != null ? user.getInstitution().getName() : null;

        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getRole(),
                institutionId,
                user.getFullName()
        );

        auditService.log(
                user.getEmail(),
                user.getRole(),
                "LOGIN_SUCCESS",
                "UserAccount",
                user.getId().toString(),
                clientIp,
                "User successfully logged in via credentials"
        );

        return new DTOs.LoginResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                institutionId,
                institutionCode,
                institutionName
        );
    }
}
