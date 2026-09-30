package com.example.honeypot_platform.dto;

import com.example.honeypot_platform.entity.Honeypot;
import com.example.honeypot_platform.entity.HoneypotStatus;
import com.example.honeypot_platform.entity.HoneypotType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HoneypotResponse {

    private Long id;
    private String name;
    private HoneypotType type;
    private String ipAddress;
    private HoneypotStatus status;
    private LocalDateTime createdAt;

    public static HoneypotResponse from(Honeypot honeypot) {
        return HoneypotResponse.builder()
                .id(honeypot.getId())
                .name(honeypot.getName())
                .type(honeypot.getType())
                .ipAddress(honeypot.getIpAddress())
                .status(honeypot.getStatus())
                .createdAt(honeypot.getCreatedAt())
                .build();
    }
}
