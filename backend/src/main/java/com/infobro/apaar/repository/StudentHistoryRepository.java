package com.infobro.apaar.repository;

import com.infobro.apaar.entity.StudentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentHistoryRepository extends JpaRepository<StudentHistory, Long> {
    List<StudentHistory> findByStudentIdOrderByChangedAtDesc(Long studentId);
    List<StudentHistory> findTop50ByOrderByChangedAtDesc();
}
