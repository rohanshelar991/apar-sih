package com.infobro.apaar.repository;

import com.infobro.apaar.entity.UploadBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadBatchRepository extends JpaRepository<UploadBatch, Long> {
    List<UploadBatch> findByInstitutionIdOrderByUploadedAtDesc(Long institutionId);
    Page<UploadBatch> findByInstitutionIdOrderByUploadedAtDesc(Long institutionId, Pageable pageable);
    List<UploadBatch> findTop10ByOrderByUploadedAtDesc();
}
