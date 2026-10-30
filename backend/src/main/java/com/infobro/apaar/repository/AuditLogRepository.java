package com.infobro.apaar.repository;

import com.infobro.apaar.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    default Page<AuditLog> findWithFilters(String actor, String role, String action, String entity,
                                          LocalDateTime fromTime, LocalDateTime toTime, Pageable pageable) {
        // Omit absent filters rather than binding untyped nulls in PostgreSQL functions.
        return findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (actor != null) predicates.add(cb.like(cb.lower(root.get("actor")), "%" + actor.toLowerCase(Locale.ROOT) + "%"));
            if (role != null) predicates.add(cb.equal(root.get("role"), role));
            if (action != null) predicates.add(cb.equal(root.get("action"), action));
            if (entity != null) predicates.add(cb.equal(root.get("entity"), entity));
            if (fromTime != null) predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), fromTime));
            if (toTime != null) predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), toTime));
            query.orderBy(cb.desc(root.get("timestamp")));
            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable);
    }

    List<AuditLog> findTop20ByOrderByTimestampDesc();
}
