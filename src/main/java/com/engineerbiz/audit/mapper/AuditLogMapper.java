package com.engineerbiz.audit.mapper;

import com.engineerbiz.audit.dto.AuditLogResponse;
import com.engineerbiz.audit.entity.AuditLog;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);

    List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs);
}
