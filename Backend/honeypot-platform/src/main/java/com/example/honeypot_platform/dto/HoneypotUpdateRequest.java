package com.example.honeypot_platform.dto;

import com.example.honeypot_platform.validation.ValidIpAddress;
import jakarta.validation.constraints.Size;

public record HoneypotUpdateRequest(
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        String type,

        @ValidIpAddress
        String ipAddress,

        String status
) {
}
