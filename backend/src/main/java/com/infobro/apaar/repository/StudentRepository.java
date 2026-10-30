package com.infobro.apaar.repository;

import com.infobro.apaar.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByApaarId(String apaarId);

    Optional<Student> findFirstByApaarId(String apaarId);

    List<Student> findByApaarIdAndAdmissionYear(String apaarId, Integer admissionYear);

    List<Student> findByApaarIdAndInstitutionIdAndProgrammeIdAndAdmissionYear(
        String apaarId, Long institutionId, Long programmeId, Integer admissionYear
    );

    Page<Student> findByInstitutionId(Long institutionId, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE " +
           "(:institutionId IS NULL OR s.institution.id = :institutionId) AND " +
           "(:programmeId IS NULL OR s.programme.id = :programmeId) AND " +
           "(:category IS NULL OR s.category = :category) AND " +
           "(:status IS NULL OR s.verificationStatus = :status) AND " +
           "(:year IS NULL OR s.admissionYear = :year) AND " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR s.apaarId LIKE CONCAT('%', :search, '%'))")
    Page<Student> findWithFilters(
        @Param("institutionId") Long institutionId,
        @Param("programmeId") Long programmeId,
        @Param("category") String category,
        @Param("status") String status,
        @Param("year") Integer year,
        @Param("search") String search,
        Pageable pageable
    );

    @Query("SELECT s FROM Student s WHERE " +
           "(:institutionId IS NULL OR s.institution.id = :institutionId) AND " +
           "(:programmeId IS NULL OR s.programme.id = :programmeId) AND " +
           "(:category IS NULL OR s.category = :category) AND " +
           "(:status IS NULL OR s.verificationStatus = :status) AND " +
           "(:year IS NULL OR s.admissionYear = :year)")
    List<Student> findAllWithFilters(
        @Param("institutionId") Long institutionId,
        @Param("programmeId") Long programmeId,
        @Param("category") String category,
        @Param("status") String status,
        @Param("year") Integer year
    );

    long countByInstitutionId(Long institutionId);

    long countByInstitutionIdAndVerificationStatus(Long institutionId, String status);

    long countByProgrammeId(Long programmeId);

    long countByProgrammeIdAndVerificationStatusIn(Long programmeId, List<String> statuses);

    @Query("SELECT s.category, COUNT(s) FROM Student s GROUP BY s.category")
    List<Object[]> countGroupedByCategory();

    @Query("SELECT s.admissionYear, COUNT(s) FROM Student s GROUP BY s.admissionYear ORDER BY s.admissionYear")
    List<Object[]> countGroupedByYear();

    @Query("SELECT s.institution.id, COUNT(s) FROM Student s GROUP BY s.institution.id")
    List<Object[]> countGroupedByInstitution();

    @Query("SELECT s FROM Student s WHERE s.apaarId IN (" +
           "SELECT s2.apaarId FROM Student s2 WHERE s2.admissionYear = :year GROUP BY s2.apaarId HAVING COUNT(DISTINCT s2.institution.id) > 1)")
    List<Student> findCrossInstitutionDuplicates(@Param("year") Integer year);

    @Query("SELECT s.email, COUNT(s) FROM Student s WHERE s.email IS NOT NULL AND s.email <> '' GROUP BY s.email HAVING COUNT(s) > 1")
    List<Object[]> findDuplicateEmails();

    @Query("SELECT s.phone, COUNT(s) FROM Student s WHERE s.phone IS NOT NULL AND s.phone <> '' GROUP BY s.phone HAVING COUNT(s) > 1")
    List<Object[]> findDuplicatePhones();
}
