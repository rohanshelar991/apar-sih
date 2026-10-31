package com.infobro.apaar.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="verification_history")
public class VerificationHistory {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="case_id",nullable=false) private VerificationCase verificationCase;
 @Column(name="from_status",length=24) private String fromStatus;
 @Column(name="to_status",nullable=false,length=24) private String toStatus;
 @Column(name="actor_email",nullable=false,length=120) private String actorEmail;
 @Column(length=1000) private String remarks;
 @Column(name="changed_at",nullable=false) private LocalDateTime changedAt=LocalDateTime.now();
 public VerificationHistory(){} public VerificationHistory(VerificationCase c,String from,String to,String actor,String remarks){verificationCase=c;fromStatus=from;toStatus=to;actorEmail=actor;this.remarks=remarks;}
 public Long getId(){return id;} public String getFromStatus(){return fromStatus;} public String getToStatus(){return toStatus;} public String getActorEmail(){return actorEmail;} public String getRemarks(){return remarks;} public LocalDateTime getChangedAt(){return changedAt;}
}
