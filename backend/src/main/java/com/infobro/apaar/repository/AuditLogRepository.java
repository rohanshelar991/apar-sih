package com.infobro.apaar.repository;

import com.infobro.apaar.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:actor IS NULL OR LOWER(a.actor) LIKE LOWER(CONCAT('%', :actor, '%'))) AND " +
           "(:role IS NULL OR a.role = :role) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:entity IS NULL OR a.entity = :entity) AND " +
           "(:fromTime IS NULL OR a.timestamp >= :fromTime) AND " +
           "(:toTime IS NULL OR a.timestamp <= :toTime) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findWithFilters(
        @Param("actor") String actor,
        @Param("role") String role,
        @Param("action") String action,
        @Param("entity") String entity,
        @Param("fromTime") LocalDateTime fromTime,
        @Param("toTime") LocalDateTime toTime,
        Pageable pageable
    );

    List<AuditLog> findTop20ByOrderByTimestampDesc();
}
