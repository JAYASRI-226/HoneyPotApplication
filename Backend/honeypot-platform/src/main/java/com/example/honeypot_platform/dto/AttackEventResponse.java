package com.example.honeypot_platform.dto;

import com.example.honeypot_platform.entity.AttackEvent;
import com.example.honeypot_platform.entity.AttackSession;

import java.time.LocalDateTime;

/** Flat, JSON-friendly view of an attack event (avoids exposing lazy JPA proxies). */
public record AttackEventResponse(
        Long id,
        String eventType,
        String eventData,
        LocalDateTime timestamp,
        String sourceIp,
        String destinationIp,
        Integer destinationPort,
        String protocol,
        String command,
        String stage,
        String severity,
        Long sessionId,
        String attackerIp,
        String honeypotName
) {
    public static AttackEventResponse from(AttackEvent e) {
        AttackSession s = e.getSession();
        String attackerIp = (s != null && s.getAttacker() != null)
                ? s.getAttacker().getIpAddress() : e.getSourceIp();
        String honeypotName = (s != null && s.getHoneypot() != null)
                ? s.getHoneypot().getName() : null;
        return new AttackEventResponse(
                e.getId(), e.getEventType(), e.getEventData(), e.getTimestamp(),
                e.getSourceIp(), e.getDestinationIp(), e.getDestinationPort(), e.getProtocol(),
                e.getCommand(),
                e.getStage() != null ? e.getStage().name() : null,
                e.getSeverity() != null ? e.getSeverity().name() : null,
                s != null ? s.getId() : null, attackerIp, honeypotName);
    }
}
