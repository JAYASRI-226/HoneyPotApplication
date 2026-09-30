package com.example.honeypot_platform.dto;

import com.example.honeypot_platform.entity.Attacker;

import java.time.LocalDateTime;

/** Flat view of an attacker with geo and computed risk. */
public record AttackerResponse(
        Long id,
        String ipAddress,
        LocalDateTime firstSeen,
        LocalDateTime lastSeen,
        Long eventCount,
        Integer totalSessions,
        String country,
        String city,
        Double latitude,
        Double longitude,
        String maxSeverity,
        Integer riskScore
) {
    public static AttackerResponse from(Attacker a) {
        return new AttackerResponse(
                a.getId(), a.getIpAddress(), a.getFirstSeen(), a.getLastSeen(),
                a.getEventCount(), a.getTotalSessions(), a.getCountry(), a.getCity(),
                a.getLatitude(), a.getLongitude(),
                a.getMaxSeverity() != null ? a.getMaxSeverity().name() : null,
                a.getRiskScore());
    }
}
