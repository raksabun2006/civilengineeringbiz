package com.engineerbiz.audit.controller;

import com.engineerbiz.audit.dto.AuditLogResponse;
import com.engineerbiz.audit.service.AuditLogService;
import com.engineerbiz.common.pagination.PagedResponse;
import com.engineerbiz.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit & Compliance Logs", description = "Immutable Transaction Auditing, Access History, and State Changes")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    @Operation(summary = "Search system audit trail with pagination and filters")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> searchAuditLogs(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) UUID performedBy,
            @PageableDefault(size = 20, sort = "performedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<AuditLogResponse> response = auditLogService.searchAuditLogs(entityName, entityId, performedBy, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/entity")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    @Operation(summary = "Get audit change history for a specific entity")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getLogsByEntity(
            @RequestParam String entityName,
            @RequestParam UUID entityId) {
        List<AuditLogResponse> response = auditLogService.getLogsByEntity(entityName, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
