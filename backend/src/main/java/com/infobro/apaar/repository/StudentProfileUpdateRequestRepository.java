package com.infobro.apaar.repository;

import com.infobro.apaar.entity.StudentProfileUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentProfileUpdateRequestRepository extends JpaRepository<StudentProfileUpdateRequest, Long> {
    List<StudentProfileUpdateRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    boolean existsByStudentIdAndStatus(Long studentId, String status);
}
