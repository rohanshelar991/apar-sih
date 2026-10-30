package com.infobro.apaar.repository;

import com.infobro.apaar.entity.Programme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgrammeRepository extends JpaRepository<Programme, Long> {
    List<Programme> findByInstitutionId(Long institutionId);
    List<Programme> findByInstitutionIdAndAcademicYear(Long institutionId, Integer academicYear);
    Optional<Programme> findByInstitutionIdAndNameIgnoreCase(Long institutionId, String name);
    Optional<Programme> findByInstitutionIdAndCodeIgnoreCase(Long institutionId, String code);
    List<Programme> findByAcademicYear(Integer academicYear);
}
