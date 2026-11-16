package com.engineerbiz.audit.dto;

import com.engineerbiz.audit.enums.AuditAction;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String entityName,
        UUID entityId,
        AuditAction action,
        UUID performedBy,
        Instant performedAt,
        String ipAddress,
        String userAgent,
        String oldValues,
        String newValues,
        String notes
) {}
