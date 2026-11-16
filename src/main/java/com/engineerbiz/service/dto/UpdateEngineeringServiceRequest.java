package com.engineerbiz.service.dto;

import com.engineerbiz.service.enums.ServiceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateEngineeringServiceRequest(
        @NotBlank(message = "Service name is required")
        @Size(max = 150)
        String name,

        @NotNull(message = "Service category is required")
        ServiceCategory category,

        String description,

        @PositiveOrZero(message = "Standard rate must be zero or positive")
        BigDecimal standardRate,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3)
        String currency,

        @Size(max = 30)
        String unitOfMeasure,

        Boolean isActive
) {}
