package com.infobro.apaar.repository;
import com.infobro.apaar.entity.Notification; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List; import java.util.Optional;
public interface NotificationRepository extends JpaRepository<Notification,Long>{ List<Notification> findTop100ByRecipientEmailOrderByCreatedAtDesc(String email); Optional<Notification> findByIdAndRecipientEmail(Long id,String email); }
