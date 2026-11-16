package com.engineerbiz.document.dto;

import com.engineerbiz.document.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateDocumentRequest(
        UUID projectId,

        @NotBlank(message = "Document title is required")
        @Size(max = 200)
        String title,

        @NotNull(message = "Document type is required")
        DocumentType documentType,

        @NotBlank(message = "File name is required")
        @Size(max = 255)
        String fileName,

        @NotBlank(message = "File URL is required")
        @Size(max = 500)
        String fileUrl,

        @NotNull(message = "File size is required")
        @Positive(message = "File size must be positive")
        Long fileSizeBytes,

        @NotBlank(message = "MIME type is required")
        @Size(max = 100)
        String mimeType,

        @Size(max = 20)
        String revision,

        String description
) {}
