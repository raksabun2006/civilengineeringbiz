package com.engineerbiz.document.dto;

import com.engineerbiz.document.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDocumentRequest(
        @NotBlank(message = "Document title is required")
        @Size(max = 200)
        String title,

        @NotNull(message = "Document type is required")
        DocumentType documentType,

        @Size(max = 20)
        String revision,

        String description
) {}
