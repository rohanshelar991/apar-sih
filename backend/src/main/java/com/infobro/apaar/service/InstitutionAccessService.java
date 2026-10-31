package com.infobro.apaar.service;

import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Objects;

/** Object authorization complements the existing route/role security rules. */
@Service
public class InstitutionAccessService {
    private final UserAccountRepository users;
    private final StudentRepository students;
    private final ProgrammeRepository programmes;
    private final ValidationIssueRepository issues;
    public InstitutionAccessService(UserAccountRepository users, StudentRepository students,
                                    ProgrammeRepository programmes, ValidationIssueRepository issues) {
        this.users=users; this.students=students; this.programmes=programmes; this.issues=issues;
    }
    public UserAccount currentUser() {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !auth.isAuthenticated()) throw denied();
        UserAccount user=users.findByEmail(auth.getName()).orElseThrow(InstitutionAccessService::denied);
        if(!Boolean.TRUE.equals(user.getActive())) throw denied();
        if(!"ROLE_INSTITUTION_ADMIN".equals(user.getRole()) && !"ROLE_AICTE_REGULATOR".equals(user.getRole()) && !"ROLE_SUPER_ADMIN".equals(user.getRole())) throw denied();
        return user;
    }
    public Long scopeInstitution(Long requested) {
        UserAccount user=currentUser();
        if("ROLE_AICTE_REGULATOR".equals(user.getRole()) || "ROLE_SUPER_ADMIN".equals(user.getRole())) return requested;
        if(user.getInstitution()==null || user.getInstitution().getId()==null) throw denied();
        Long own=user.getInstitution().getId();
        if(requested!=null && !own.equals(requested)) throw denied();
        return own;
    }
    public Long requireInstitution(Long requested) {
        Long scoped=scopeInstitution(requested);
        if(scoped==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Institution selection is required.");
        return scoped;
    }
    public void requireStudent(Long id) {
        Student student=students.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Student not found."));
        if(student.getInstitution()==null) throw denied();
        requireInstitution(student.getInstitution().getId());
    }
    public void requireProgramme(Long id, Long institutionId) {
        requireInstitution(institutionId);
        if(id==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Programme is required.");
        Programme programme=programmes.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Programme not found."));
        if(programme.getInstitution()==null || !Objects.equals(institutionId,programme.getInstitution().getId())) throw denied();
    }
    public void requireIssue(Long id) {
        ValidationIssue issue=issues.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Issue not found."));
        UserAccount user=currentUser();
        if(issue.getStudent()!=null) requireStudent(issue.getStudent().getId());
        if(issue.getUploadBatch()!=null) requireInstitution(issue.getUploadBatch().getInstitution().getId());
        if(issue.getStudent()==null && issue.getUploadBatch()==null && !"ROLE_AICTE_REGULATOR".equals(user.getRole()) && !"ROLE_SUPER_ADMIN".equals(user.getRole())) throw denied();
    }
    private static AccessDeniedException denied() { return new AccessDeniedException("Access denied for institution."); }
}
