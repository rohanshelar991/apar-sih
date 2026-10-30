package com.infobro.apaar.service;

import com.infobro.apaar.dto.DTOs;
import com.infobro.apaar.entity.AuditLog;
import com.infobro.apaar.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String actor, String role, String action, String entity, String entityId, String ip, String details) {
        AuditLog auditLog = new AuditLog(
                actor != null ? actor : "SYSTEM",
                role != null ? role : "ROLE_SYSTEM",
                action,
                entity,
                entityId,
                ip,
                details
        );
        auditLogRepository.save(auditLog);
    }

    public Page<DTOs.AuditLogDto> getAuditLogs(String actor, String role, String action, String entity,
                                               LocalDateTime fromTime, LocalDateTime toTime, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findWithFilters(actor, role, action, entity, fromTime, toTime, pageable)
                .map(this::toDto);
    }

    public String exportAuditLogsCsv(String actor, String role, String action, String entity) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Timestamp,Actor,Role,Action,Entity,EntityID,IP,Details");

        Page<AuditLog> page = auditLogRepository.findWithFilters(actor, role, action, entity, null, null, PageRequest.of(0, 1000));
        for (AuditLog log : page.getContent()) {
            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    log.getTimestamp(),
                    escapeCsv(log.getActor()),
                    escapeCsv(log.getRole()),
                    escapeCsv(log.getAction()),
                    escapeCsv(log.getEntity()),
                    escapeCsv(log.getEntityId()),
                    escapeCsv(log.getIp()),
                    escapeCsv(log.getDetails())
            );
        }
        return sw.toString();
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }

    private DTOs.AuditLogDto toDto(AuditLog log) {
        DTOs.AuditLogDto dto = new DTOs.AuditLogDto();
        dto.setId(log.getId());
        dto.setActor(log.getActor());
        dto.setRole(log.getRole());
        dto.setAction(log.getAction());
        dto.setEntity(log.getEntity());
        dto.setEntityId(log.getEntityId());
        dto.setIp(log.getIp());
        dto.setTimestamp(log.getTimestamp());
        dto.setDetails(log.getDetails());
        return dto;
    }
}
