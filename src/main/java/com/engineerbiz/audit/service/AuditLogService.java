package com.engineerbiz.audit.service;

import com.engineerbiz.audit.dto.AuditLogResponse;
import com.engineerbiz.audit.enums.AuditAction;
import com.engineerbiz.common.pagination.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AuditLogService {

    void logAction(String entityName, UUID entityId, AuditAction action, UUID performedBy,
                   String ipAddress, String userAgent, String oldValues, String newValues, String notes);

    List<AuditLogResponse> getLogsByEntity(String entityName, UUID entityId);

    PagedResponse<AuditLogResponse> searchAuditLogs(String entityName, UUID entityId, UUID performedBy, Pageable pageable);
}
