package com.example.honeypot_platform.dto;

import com.example.honeypot_platform.entity.AttackSession;
import com.example.honeypot_platform.entity.Attacker;
import com.example.honeypot_platform.entity.Honeypot;

import java.time.LocalDateTime;

/** Flat view of a correlated attack session, including its kill-chain profile. */
public record AttackSessionResponse(
        Long id,
        String sensorSessionId,
        String status,
        String protocol,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer eventCount,
        String stages,
        String maxSeverity,
        String commands,
        boolean multiStage,
        Integer riskScore,
        String attackType,
        Long attackerId,
        String attackerIp,
        String attackerCountry,
        Long honeypotId,
        String honeypotName
) {
    public static AttackSessionResponse from(AttackSession s) {
        Attacker a = s.getAttacker();
        Honeypot h = s.getHoneypot();
        return new AttackSessionResponse(
                s.getId(), s.getSensorSessionId(), s.getStatus(), s.getProtocol(),
                s.getStartTime(), s.getEndTime(), s.getEventCount(), s.getStages(),
                s.getMaxSeverity() != null ? s.getMaxSeverity().name() : null,
                s.getCommands(), s.isMultiStage(), s.getRiskScore(), s.getAttackType(),
                a != null ? a.getId() : null,
                a != null ? a.getIpAddress() : null,
                a != null ? a.getCountry() : null,
                h != null ? h.getId() : null,
                h != null ? h.getName() : null);
    }
}
