package com.example.honeypot_platform.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Top-level metrics for the dashboard header and summary cards. */
public record SummaryResponse(
        long honeypots,
        long activeHoneypots,
        long attackers,
        long sessions,
        long activeSessions,
        long multiStageSessions,
        long events,
        long alerts,
        long newAlerts,
        long criticalAlerts,
        List<NameCount> severityBreakdown,
        List<NameCount> stageBreakdown,
        String topAttackType,
        String topAttackerIp,
        int highestRisk,
        LocalDateTime lastEventAt
) {
}
