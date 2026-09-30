package com.example.honeypot_platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** JSON body for PUT /api/users/{id}. Omit any field you do not want to change. */
public record UserUpdateRequest(
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Email(message = "Email must be a valid address, for example asha@example.com")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        String role
) {
}
