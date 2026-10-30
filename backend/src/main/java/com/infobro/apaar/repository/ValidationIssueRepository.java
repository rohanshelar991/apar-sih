package com.infobro.apaar.repository;

import com.infobro.apaar.entity.ValidationIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationIssueRepository extends JpaRepository<ValidationIssue, Long> {

    List<ValidationIssue> findByStudentId(Long studentId);

    List<ValidationIssue> findByUploadBatchId(Long batchId);

    List<ValidationIssue> findByStatus(String status);

    long countByStatus(String status);

    long countByStatusAndStudentInstitutionId(String status, Long institutionId);

    @Query("SELECT vi FROM ValidationIssue vi WHERE " +
           "(:institutionId IS NULL OR (vi.student IS NOT NULL AND vi.student.institution.id = :institutionId) OR (vi.uploadBatch IS NOT NULL AND vi.uploadBatch.institution.id = :institutionId)) AND " +
           "(:status IS NULL OR vi.status = :status) AND " +
           "(:severity IS NULL OR vi.severity = :severity) AND " +
           "(:ruleCode IS NULL OR vi.ruleCode = :ruleCode)")
    Page<ValidationIssue> findWithFilters(
        @Param("institutionId") Long institutionId,
        @Param("status") String status,
        @Param("severity") String severity,
        @Param("ruleCode") String ruleCode,
        Pageable pageable
    );

    @Query("SELECT vi FROM ValidationIssue vi WHERE " +
           "vi.status = 'OPEN' AND " +
           "(vi.ruleCode = 'DUPLICATE_CROSS_INSTITUTION' OR vi.ruleCode = 'DUPLICATE_SAME_INSTITUTION' OR vi.ruleCode = 'INCONSISTENT_DATA')")
    List<ValidationIssue> findOpenAnomalies();
}
