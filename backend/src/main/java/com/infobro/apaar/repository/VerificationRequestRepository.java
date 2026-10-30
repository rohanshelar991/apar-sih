package com.infobro.apaar.repository;

import com.infobro.apaar.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    List<VerificationRequest> findByTokenIdOrderByRequestedAtDesc(Long tokenId);
    List<VerificationRequest> findTop20ByOrderByRequestedAtDesc();
}
