package com.infobro.apaar.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="notifications")
public class Notification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="recipient_email",nullable=false,length=120) private String recipientEmail;
 @Column(name="notification_type",nullable=false,length=32) private String notificationType;
 @Column(nullable=false,length=160) private String title;
 @Column(nullable=false,length=500) private String message;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="related_case_id") private VerificationCase relatedCase;
 @Column(name="read_at") private LocalDateTime readAt;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public Notification(){} public Notification(String recipient,String type,String title,String message,VerificationCase c){recipientEmail=recipient;notificationType=type;this.title=title;this.message=message;relatedCase=c;}
 public Long getId(){return id;} public String getNotificationType(){return notificationType;} public String getTitle(){return title;} public String getMessage(){return message;} public Long getCaseId(){return relatedCase==null?null:relatedCase.getId();} public LocalDateTime getReadAt(){return readAt;} public LocalDateTime getCreatedAt(){return createdAt;} public void setReadAt(LocalDateTime v){readAt=v;}
}
