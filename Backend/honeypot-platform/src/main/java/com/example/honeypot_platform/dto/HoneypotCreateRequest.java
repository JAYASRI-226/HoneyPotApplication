package com.example.honeypot_platform.dto;

import com.example.honeypot_platform.validation.ValidIpAddress;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HoneypotCreateRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Type is required. Use SSH, TELNET, HTTP, or WEB")
        String type,

        @NotBlank(message = "IP address is required")
        @ValidIpAddress
        String ipAddress,

        @NotBlank(message = "Status is required. Use ACTIVE, INACTIVE, or OFFLINE")
        String status
) {
}
