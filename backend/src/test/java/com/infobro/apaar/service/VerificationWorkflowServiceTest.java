package com.infobro.apaar.service;

import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class VerificationWorkflowServiceTest {
 @Test void reviewerDecisionIsRecordedAndRejectedResubmissionCanBeRepresented() {
  Institution institution=new Institution("TST","Test Institute","State","City","ACTIVE"); institution.setId(4L);
  Programme programme=new Programme(institution,"Test Programme","TP",2026,40,0); programme.setId(8L);
  Student student=new Student("202610101010","Fictional Student", LocalDate.of(2005,1,1),"Other",programme,institution,2024,"REGULAR",null,null,"PENDING",true);student.setId(5L);
  VerificationCase c=new VerificationCase();c.setStudent(student);c.setInstitution(institution);c.setSubmittedBy("admin@example.test");c.setStatus("PENDING");
  UserAccount reviewer=new UserAccount("reviewer@example.test","unused-hash","Reviewer","ROLE_AUTHORIZED_VERIFIER",null);
  List<VerificationHistory> historyRows=new ArrayList<>();
  VerificationCaseRepository cases=proxy(VerificationCaseRepository.class,(m,a)-> switch(m){case "findById"->Optional.of(c);case "save"->a[0];default->null;});
  VerificationHistoryRepository histories=proxy(VerificationHistoryRepository.class,(m,a)->{if(m.equals("save")){historyRows.add((VerificationHistory)a[0]);return a[0];}return null;});
  NotificationRepository notices=proxy(NotificationRepository.class,(m,a)->a[0]);
  UserAccountRepository users=proxy(UserAccountRepository.class,(m,a)->m.equals("findByEmail")?Optional.of(reviewer):null);
  AuditLogRepository auditRows=proxy(AuditLogRepository.class,(m,a)->null);
  InstitutionAccessService access=new InstitutionAccessService(null,null,null,null);
  VerificationWorkflowService service=new VerificationWorkflowService(cases,histories,notices,null,users,access,new AuditService(auditRows));

  Map<String,Object> result=service.decide(17L,"APPROVED","Reviewed source record.",reviewer.getEmail(),null);
  assertEquals("APPROVED",result.get("status"));
  assertEquals("APPROVED",c.getStatus());
  assertEquals(1,historyRows.size());
  assertEquals("PENDING",historyRows.get(0).getFromStatus());
  assertEquals("APPROVED",historyRows.get(0).getToStatus());
 }

 @Test void rejectionRequiresReason() {
  UserAccount reviewer=new UserAccount("reviewer@example.test","unused-hash","Reviewer","ROLE_AUTHORIZED_VERIFIER",null);
  UserAccountRepository users=proxy(UserAccountRepository.class,(m,a)->Optional.of(reviewer));
  VerificationWorkflowService service=new VerificationWorkflowService(null,null,null,null,users,null,null);
  ResponseStatusException ex=assertThrows(ResponseStatusException.class,()->service.decide(1L,"REJECTED","  ",reviewer.getEmail(),null));
  assertEquals(400,ex.getStatusCode().value());
 }

 @Test void institutionAdminCannotMakeReviewerDecision() {
  UserAccount admin=new UserAccount("admin@example.test","unused-hash","Admin","ROLE_INSTITUTION_ADMIN",null);
  UserAccountRepository users=proxy(UserAccountRepository.class,(m,a)->Optional.of(admin));
  VerificationWorkflowService service=new VerificationWorkflowService(null,null,null,null,users,null,null);
  assertThrows(org.springframework.security.access.AccessDeniedException.class,
      ()->service.decide(1L,"APPROVED","Reviewed",admin.getEmail(),null));
 }
 private interface Invocation { Object call(String method,Object[] args); }
 @SuppressWarnings("unchecked") private static <T>T proxy(Class<T> type,Invocation invocation){return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},(p,m,a)->invocation.call(m.getName(),a==null?new Object[0]:a));}
}
