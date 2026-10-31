package com.infobro.apaar.service;

import com.infobro.apaar.entity.*;
import com.infobro.apaar.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VerificationWorkflowService {
 private static final List<String> ACTIVE=List.of("PENDING","UNDER_REVIEW");
 private final VerificationCaseRepository cases; private final VerificationHistoryRepository history; private final NotificationRepository notifications;
 private final StudentRepository students; private final UserAccountRepository users; private final InstitutionAccessService access; private final AuditService audit;
 public VerificationWorkflowService(VerificationCaseRepository c,VerificationHistoryRepository h,NotificationRepository n,StudentRepository s,UserAccountRepository u,InstitutionAccessService a,AuditService audit){cases=c;history=h;notifications=n;students=s;users=u;access=a;this.audit=audit;}
 @Transactional public Map<String,Object> submit(Long studentId,String actor,String ip){
  UserAccount user=active(actor); Student student=students.findById(studentId).orElseThrow(()->missing("Student not found."));
  if("ROLE_STUDENT".equals(user.getRole())) { if(user.getStudent()==null || !Objects.equals(user.getStudent().getId(), studentId)) throw denied(); }
  else if(isInstitutionAdmin(user.getRole())) access.requireStudent(studentId); else throw denied();
  if(cases.existsByApaarIdAndStatusIn(student.getApaarId(),ACTIVE)) throw new ResponseStatusException(HttpStatus.CONFLICT,"An active verification request already exists for this student identifier.");
  VerificationCase c=new VerificationCase(); c.setStudent(student); c.setInstitution(student.getInstitution()); c.setSubmittedBy(actor); c.setStatus("PENDING"); c=cases.save(c);
  history.save(new VerificationHistory(c,null,"PENDING",actor,"Submitted for institutional review."));
  String recipient=users.findByStudentId(student.getId()).map(UserAccount::getEmail).orElse(actor);
  notifications.save(new Notification(recipient,"VERIFICATION_SUBMITTED","Request submitted","Verification request #"+c.getId()+" is pending review.",c));
  audit.log(actor,user.getRole(),"VERIFICATION_SUBMITTED","VerificationCase",c.getId().toString(),ip,"Submitted verification case for student record "+studentId);
  return caseDto(c);
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> queue(String actor){
  UserAccount user=active(actor); List<VerificationCase> rows;
  if("ROLE_INSTITUTION_ADMIN".equals(user.getRole())) { if(user.getInstitution()==null)throw denied(); rows=cases.findByInstitution(user.getInstitution().getId()); }
  else if("ROLE_STUDENT".equals(user.getRole())) { if(user.getStudent()==null)throw denied(); rows=cases.findByStudent(user.getStudent().getId()); }
  else if(isReviewer(user.getRole())) rows=cases.findAllForQueue(); else throw denied();
  return rows.stream().map(this::caseDto).toList();
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> history(Long id,String actor){ VerificationCase c=accessible(id,actor); return history.findByVerificationCaseIdOrderByChangedAtDesc(c.getId()).stream().map(h->{Map<String,Object> m=new LinkedHashMap<>();m.put("id",h.getId());m.put("fromStatus",h.getFromStatus());m.put("toStatus",h.getToStatus());m.put("actorEmail",h.getActorEmail());m.put("remarks",h.getRemarks());m.put("changedAt",h.getChangedAt());return m;}).toList(); }
 @Transactional public Map<String,Object> decide(Long id,String decision,String remarks,String actor,String ip){
  UserAccount user=active(actor);if(!isReviewer(user.getRole()))throw denied();
  if(!List.of("APPROVED","REJECTED","UNDER_REVIEW").contains(decision))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Decision must be UNDER_REVIEW, APPROVED or REJECTED.");
  if("REJECTED".equals(decision)&&(remarks==null||remarks.isBlank()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"A reason is required when rejecting a request.");
  VerificationCase c=cases.findById(id).orElseThrow(()->missing("Verification request not found."));if(!ACTIVE.contains(c.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"This request has already been decided.");
  String old=c.getStatus(); c.setStatus(decision);c.setReviewerEmail(actor);c.setRemarks(clean(remarks));c.setUpdatedAt(LocalDateTime.now());
  VerificationCase saved=cases.save(c);history.save(new VerificationHistory(saved,old,decision,actor,clean(remarks)));
  notifications.save(new Notification(saved.getSubmittedBy(),"VERIFICATION_"+decision,"Verification "+decision.toLowerCase(),"Request #"+id+" was "+decision.toLowerCase()+ (remarks==null||remarks.isBlank()?".":": "+clean(remarks)),saved));
  audit.log(actor,user.getRole(),"VERIFICATION_"+decision,"VerificationCase",id.toString(),ip,"Recorded review decision "+decision+" for student record "+c.getStudent().getId());return caseDto(saved);
 }
 @Transactional public Map<String,Object> assign(Long id,String assignee,String priority,String actor,String ip){
  UserAccount user=active(actor); if(!isReviewer(user.getRole()) && !"ROLE_INSTITUTION_ADMIN".equals(user.getRole()))throw denied();
  VerificationCase c=cases.findById(id).orElseThrow(()->missing("Verification request not found."));
  if("ROLE_INSTITUTION_ADMIN".equals(user.getRole())) access.requireStudent(c.getStudent().getId());
  if(!ACTIVE.contains(c.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Only active requests may be assigned.");
  UserAccount reviewer=users.findByEmail(assignee==null?"":assignee.trim().toLowerCase(Locale.ROOT)).filter(x->Boolean.TRUE.equals(x.getActive())&&isReviewer(x.getRole())).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Select an active verification officer."));
  if("ROLE_INSTITUTION_ADMIN".equals(user.getRole())&&reviewer.getInstitution()!=null&&!Objects.equals(reviewer.getInstitution().getId(),c.getInstitution().getId()))throw denied();
  if(priority!=null&&!List.of("LOW","NORMAL","HIGH","URGENT").contains(priority))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Priority must be LOW, NORMAL, HIGH or URGENT.");
  c.setAssignedTo(reviewer.getEmail()); if(priority!=null)c.setPriority(priority); c.setUpdatedAt(LocalDateTime.now()); c=cases.save(c);
  history.save(new VerificationHistory(c,c.getStatus(),c.getStatus(),actor,"Assigned to a verification officer; priority "+c.getPriority()+"."));
  notifications.save(new Notification(reviewer.getEmail(),"VERIFICATION_ASSIGNED","Verification request assigned","Request #"+id+" has been assigned to you.",c));
  audit.log(actor,user.getRole(),"VERIFICATION_ASSIGNED","VerificationCase",id.toString(),ip,"Assigned request and set priority "+c.getPriority()+"."); return caseDto(c);
 }
 @Transactional public List<Map<String,Object>> assignBatch(List<Long> ids,String assignee,String priority,String actor,String ip){
  if(ids==null||ids.isEmpty()||ids.size()>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Select between 1 and 100 requests.");
  return ids.stream().distinct().map(id->assign(id,assignee,priority,actor,ip)).toList();
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> myNotifications(String actor){active(actor);return notifications.findTop100ByRecipientEmailOrderByCreatedAtDesc(actor).stream().map(n->{Map<String,Object> m=new LinkedHashMap<>();m.put("id",n.getId());m.put("type",n.getNotificationType());m.put("title",n.getTitle());m.put("message",n.getMessage());m.put("caseId",n.getCaseId());m.put("readAt",n.getReadAt());m.put("createdAt",n.getCreatedAt());return m;}).toList();}
 @Transactional public void markRead(Long id,String actor){UserAccount u=active(actor);Notification n=notifications.findByIdAndRecipientEmail(id,actor).orElseThrow(()->missing("Notification not found."));if(n.getReadAt()==null){n.setReadAt(LocalDateTime.now());notifications.save(n);audit.log(actor,u.getRole(),"NOTIFICATION_READ","Notification",id.toString(),null,"Marked an in-app notification as read.");}}
 private VerificationCase accessible(Long id,String actor){UserAccount u=active(actor);VerificationCase c=cases.findById(id).orElseThrow(()->missing("Verification request not found."));if("ROLE_INSTITUTION_ADMIN".equals(u.getRole())){if(u.getInstitution()==null||!Objects.equals(u.getInstitution().getId(),c.getInstitution().getId()))throw denied();}else if("ROLE_STUDENT".equals(u.getRole())){if(u.getStudent()==null||!Objects.equals(u.getStudent().getId(),c.getStudent().getId()))throw denied();}else if(!isReviewer(u.getRole()))throw denied();return c;}
 private UserAccount active(String actor){UserAccount u=users.findByEmail(actor).orElseThrow(VerificationWorkflowService::denied);if(!Boolean.TRUE.equals(u.getActive()))throw denied();return u;}
 private static boolean isReviewer(String role){return "ROLE_AUTHORIZED_VERIFIER".equals(role)||"ROLE_VERIFICATION_OFFICER".equals(role)||"ROLE_AICTE_REGULATOR".equals(role)||"ROLE_SUPER_ADMIN".equals(role);}
 private static boolean isInstitutionAdmin(String role){return "ROLE_INSTITUTION_ADMIN".equals(role)||"ROLE_AICTE_REGULATOR".equals(role)||"ROLE_SUPER_ADMIN".equals(role);}
 private static org.springframework.security.access.AccessDeniedException denied(){return new org.springframework.security.access.AccessDeniedException("Access denied.");}
 private static ResponseStatusException missing(String msg){return new ResponseStatusException(HttpStatus.NOT_FOUND,msg);}
 private static String clean(String s){if(s==null)return null;String v=s.trim();return v.length()>1000?v.substring(0,1000):v;}
 private Map<String,Object> caseDto(VerificationCase c){Map<String,Object> m=new LinkedHashMap<>();m.put("id",c.getId());m.put("studentId",c.getStudent().getId());m.put("studentName",c.getStudent().getName());m.put("maskedApaarId",c.getStudent().getMaskedApaarId());m.put("institutionId",c.getInstitution().getId());m.put("institutionName",c.getInstitution().getName());m.put("programme",c.getStudent().getProgramme().getName());m.put("status",c.getStatus());m.put("priority",c.getPriority());m.put("assignedTo",c.getAssignedTo());m.put("submittedBy",c.getSubmittedBy());m.put("reviewerEmail",c.getReviewerEmail());m.put("remarks",c.getRemarks());m.put("submittedAt",c.getSubmittedAt());m.put("updatedAt",c.getUpdatedAt());return m;}
}
