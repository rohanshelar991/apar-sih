package com.infobro.apaar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_requests", indexes = {
    @Index(name = "idx_vreq_token", columnList = "token_id"),
    @Index(name = "idx_vreq_time", columnList = "requested_at")
})
public class VerificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_id", nullable = false)
    private VerificationToken token;

    @Column(name = "verifier_ip", length = 45)
    private String verifierIp;

    @Column(name = "verifier_agent", length = 255)
    private String verifierAgent;

    @Column(nullable = false, length = 32)
    private String status = "SUCCESS";

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    public VerificationRequest() {}

    public VerificationRequest(VerificationToken token, String verifierIp, String verifierAgent, String status) {
        this.token = token;
        this.verifierIp = verifierIp;
        this.verifierAgent = verifierAgent;
        this.status = status;
        this.requestedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VerificationToken getToken() { return token; }
    public void setToken(VerificationToken token) { this.token = token; }

    public String getVerifierIp() { return verifierIp; }
    public void setVerifierIp(String verifierIp) { this.verifierIp = verifierIp; }

    public String getVerifierAgent() { return verifierAgent; }
    public void setVerifierAgent(String verifierAgent) { this.verifierAgent = verifierAgent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
}
