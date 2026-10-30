package com.infobro.apaar.repository;

import com.infobro.apaar.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Locale;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    List<Student> findByApaarId(String apaarId);

    Optional<Student> findFirstByApaarId(String apaarId);

    List<Student> findByApaarIdAndAdmissionYear(String apaarId, Integer admissionYear);

    List<Student> findByApaarIdAndInstitutionIdAndProgrammeIdAndAdmissionYear(
        String apaarId, Long institutionId, Long programmeId, Integer admissionYear
    );

    List<Student> findByApaarIdAndInstitutionIdAndAdmissionYearAndVerificationStatusIn(
        String apaarId, Long institutionId, Integer admissionYear, List<String> statuses
    );

    Page<Student> findByInstitutionId(Long institutionId, Pageable pageable);

    default Page<Student> findWithFilters(Long institutionId, Long programmeId, String category,
                                          String status, Integer year, String search, Pageable pageable) {
        return findAll(filters(institutionId, programmeId, category, status, year, search), pageable);
    }

    default List<Student> findAllWithFilters(Long institutionId, Long programmeId, String category,
                                             String status, Integer year) {
        return findAll(filters(institutionId, programmeId, category, status, year, null));
    }

    private static Specification<Student> filters(Long institutionId, Long programmeId, String category,
                                                   String status, Integer year, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (institutionId != null) predicates.add(cb.equal(root.get("institution").get("id"), institutionId));
            if (programmeId != null) predicates.add(cb.equal(root.get("programme").get("id"), programmeId));
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (status != null) predicates.add(cb.equal(root.get("verificationStatus"), status));
            if (year != null) predicates.add(cb.equal(root.get("admissionYear"), year));
            if (search != null) predicates.add(cb.or(
                cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase(Locale.ROOT) + "%"),
                cb.like(root.get("apaarId"), "%" + search + "%")));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

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

    @Query("SELECT s FROM Student s WHERE s.admissionYear = :year AND s.apaarId IN (" +
           "SELECT s2.apaarId FROM Student s2 WHERE s2.admissionYear = :year GROUP BY s2.apaarId HAVING COUNT(DISTINCT s2.institution.id) > 1)")
    List<Student> findCrossInstitutionDuplicates(@Param("year") Integer year);

    @Query("SELECT s.email, COUNT(s) FROM Student s WHERE s.email IS NOT NULL AND s.email <> '' GROUP BY s.email HAVING COUNT(s) > 1")
    List<Object[]> findDuplicateEmails();

    @Query("SELECT s.phone, COUNT(s) FROM Student s WHERE s.phone IS NOT NULL AND s.phone <> '' GROUP BY s.phone HAVING COUNT(s) > 1")
    List<Object[]> findDuplicatePhones();
}
