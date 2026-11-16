package com.engineerbiz.audit.service;

import com.engineerbiz.audit.dto.AuditLogResponse;
import com.engineerbiz.audit.entity.AuditLog;
import com.engineerbiz.audit.enums.AuditAction;
import com.engineerbiz.audit.mapper.AuditLogMapper;
import com.engineerbiz.audit.repository.AuditLogRepository;
import com.engineerbiz.audit.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private AuditLogMapper auditLogMapper;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogServiceImpl(auditLogRepository, auditLogMapper);
    }

    @Test
    @DisplayName("Should successfully record audit log entry")
    void testLogAction() {
        UUID entityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        auditLogService.logAction(
                "Contract",
                entityId,
                AuditAction.APPROVAL,
                userId,
                "192.168.1.50",
                "Mozilla/5.0",
                "{\"status\":\"DRAFT\"}",
                "{\"status\":\"CLIENT_SIGNED\"}",
                "Contract signed by authorized director"
        );

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should retrieve audit history for entity")
    void testGetLogsByEntity() {
        UUID entityId = UUID.randomUUID();

        AuditLog logEntry = new AuditLog();
        logEntry.setId(UUID.randomUUID());
        logEntry.setEntityName("Contract");
        logEntry.setEntityId(entityId);
        logEntry.setAction(AuditAction.INSERT);

        when(auditLogRepository.findByEntityNameAndEntityIdOrderByPerformedAtDesc("Contract", entityId))
                .thenReturn(List.of(logEntry));

        AuditLogResponse response = new AuditLogResponse(
                logEntry.getId(), "Contract", entityId, AuditAction.INSERT,
                UUID.randomUUID(), Instant.now(), "127.0.0.1", "Agent", null, "{}", "Notes"
        );
        when(auditLogMapper.toResponse(logEntry)).thenReturn(response);

        List<AuditLogResponse> logs = auditLogService.getLogsByEntity("Contract", entityId);

        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().action()).isEqualTo(AuditAction.INSERT);
    }
}
