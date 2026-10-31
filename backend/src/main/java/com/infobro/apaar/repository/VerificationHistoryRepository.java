package com.infobro.apaar.repository;
import com.infobro.apaar.entity.VerificationHistory; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface VerificationHistoryRepository extends JpaRepository<VerificationHistory,Long>{ List<VerificationHistory> findByVerificationCaseIdOrderByChangedAtDesc(Long caseId); }
