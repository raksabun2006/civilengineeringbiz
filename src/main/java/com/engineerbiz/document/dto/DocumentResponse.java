package com.engineerbiz.document.dto;

import com.engineerbiz.document.enums.DocumentType;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String documentCode,
        String title,
        DocumentType documentType,
        UUID projectId,
        String projectCode,
        String projectName,
        String fileName,
        String fileUrl,
        Long fileSizeBytes,
        String mimeType,
        String revision,
        String description,
        UUID uploadedBy,
        Instant createdAt,
        Instant updatedAt
) {}
