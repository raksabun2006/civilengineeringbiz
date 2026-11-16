package com.engineerbiz.audit.service.impl;

import com.engineerbiz.audit.dto.AuditLogResponse;
import com.engineerbiz.audit.entity.AuditLog;
import com.engineerbiz.audit.enums.AuditAction;
import com.engineerbiz.audit.mapper.AuditLogMapper;
import com.engineerbiz.audit.repository.AuditLogRepository;
import com.engineerbiz.audit.service.AuditLogService;
import com.engineerbiz.common.pagination.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String entityName, UUID entityId, AuditAction action, UUID performedBy,
                          String ipAddress, String userAgent, String oldValues, String newValues, String notes) {
        try {
            AuditLog logEntry = new AuditLog();
            logEntry.setEntityName(entityName);
            logEntry.setEntityId(entityId);
            logEntry.setAction(action);
            logEntry.setPerformedBy(performedBy);
            logEntry.setPerformedAt(Instant.now());
            logEntry.setIpAddress(ipAddress);
            logEntry.setUserAgent(userAgent);
            logEntry.setOldValues(oldValues);
            logEntry.setNewValues(newValues);
            logEntry.setNotes(notes);

            auditLogRepository.save(logEntry);
            log.debug("Recorded audit log: [{}] on [{}:{}] by [{}]", action, entityName, entityId, performedBy);
        } catch (Exception e) {
            log.error("Failed to record audit log for entity [{}:{}]", entityName, entityId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByEntity(String entityName, UUID entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByPerformedAtDesc(entityName, entityId).stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> searchAuditLogs(String entityName, UUID entityId, UUID performedBy, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.searchAuditLogs(entityName, entityId, performedBy, pageable);
        return PagedResponse.from(page.map(auditLogMapper::toResponse));
    }
}
