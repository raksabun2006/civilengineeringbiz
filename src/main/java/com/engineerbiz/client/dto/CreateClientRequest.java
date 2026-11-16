package com.engineerbiz.client.dto;

import com.engineerbiz.client.enums.ClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateClientRequest(
        @NotBlank(message = "Client name is required")
        @Size(max = 150)
        String name,

        @Size(max = 150)
        String nameKhmer,

        @NotNull(message = "Client type is required")
        ClientType clientType,

        @Size(max = 50)
        String vatTin,

        @Size(max = 100)
        String primaryContactName,

        @Size(max = 150)
        String primaryContactEmail,

        @NotBlank(message = "Primary contact phone is required")
        @Size(max = 30)
        String primaryContactPhone,

        @Size(max = 255)
        String billingAddress,

        @Size(max = 100)
        String city,

        Integer paymentTermDays,

        BigDecimal creditLimit,

        String notes
) {}
