package com.engineerbiz.quotation.dto;

import com.engineerbiz.quotation.enums.QuotationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for updating quotation status")
public record UpdateQuotationStatusRequest(
        @NotNull(message = "Status is required")
        @Schema(description = "Target quotation status", example = "SENT")
        QuotationStatus status
) {}
