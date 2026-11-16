package com.engineerbiz.identity.dto;

import com.engineerbiz.identity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Full name is required")
        @Size(max = 100)
        String fullName,

        @Size(max = 30)
        String phone,

        @Size(max = 30)
        String phoneNumber,

        UserRole role,

        @Size(max = 100)
        String jobTitle,

        Set<String> permissions
) {
    public CreateUserRequest(String email, String password, String fullName, String phone, String jobTitle, UserRole role, Set<String> permissions) {
        this(email, fullName, phone, null, role, jobTitle, permissions);
    }

    public String resolvePhone() {
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            return phoneNumber.trim();
        }
        if (phone != null && !phone.isBlank()) {
            return phone.trim();
        }
        return null;
    }
}
