package com.example.honeypot_platform.dto;

import java.util.List;

/**
 * Full multi-stage view of one attack session: the session profile, its ordered
 * kill-chain stages (each with its events), the raw event timeline, and any alerts raised.
 */
public record AttackSessionDetailResponse(
        AttackSessionResponse session,
        List<StageGroup> chain,
        List<AttackEventResponse> events,
        List<AlertResponse> alerts
) {
    /** One kill-chain stage and the events that belong to it. */
    public record StageGroup(
            String stage,
            String label,
            int order,
            long eventCount,
            String maxSeverity,
            List<AttackEventResponse> events
    ) {
    }
}
