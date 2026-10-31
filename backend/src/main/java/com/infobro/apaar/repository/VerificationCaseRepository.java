package com.infobro.apaar.repository;
import com.infobro.apaar.entity.VerificationCase; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Query; import java.util.List;
public interface VerificationCaseRepository extends JpaRepository<VerificationCase,Long>{
 boolean existsByStudentIdAndStatusIn(Long studentId,List<String> statuses);
 @Query("select case when count(c)>0 then true else false end from VerificationCase c where c.student.apaarId=:apaarId and c.status in :statuses") boolean existsByApaarIdAndStatusIn(String apaarId,List<String> statuses);
 @Query("select c from VerificationCase c join fetch c.student s join fetch c.institution i order by c.submittedAt desc") List<VerificationCase> findAllForQueue();
 @Query("select c from VerificationCase c join fetch c.student s join fetch c.institution i where c.institution.id=:institutionId order by c.submittedAt desc") List<VerificationCase> findByInstitution(Long institutionId);
 @Query("select c from VerificationCase c join fetch c.student s join fetch c.institution i where c.student.id=:studentId order by c.submittedAt desc") List<VerificationCase> findByStudent(Long studentId);
}
