package com.engineerbiz.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyProfileRequest(
        @NotBlank(message = "English company name is required")
        @Size(max = 150)
        String companyNameEn,

        @Size(max = 150)
        String companyNameKm,

        @Size(max = 50)
        String registrationNumber,

        @Size(max = 50)
        String vatTin,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Phone number is required")
        @Size(max = 30)
        String phone,

        @Size(max = 150)
        String website,

        @NotBlank(message = "Address is required")
        @Size(max = 255)
        String addressLine1,

        @Size(max = 255)
        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @NotBlank(message = "Country is required")
        @Size(max = 60)
        String country,

        @NotBlank(message = "Default currency is required")
        @Size(min = 3, max = 3)
        String defaultCurrency,

        String logoUrl,

        @Size(max = 100)
        String bankName,

        @Size(max = 100)
        String bankAccountName,

        @Size(max = 50)
        String bankAccountNumber,

        @Size(max = 20)
        String bankSwiftCode,

        @Size(max = 100)
        String bakongId
) {}
