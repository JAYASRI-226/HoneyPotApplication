package com.example.honeypot_platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One attack session between an attacker and a honeypot (a Cowrie "session").
 * The detection engine enriches this with kill-chain stages, severity and risk.
 */
@Entity
@Table(name = "attack_sessions", indexes = {
        @Index(name = "idx_session_attacker", columnList = "attacker_id"),
        @Index(name = "idx_session_start", columnList = "startTime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttackSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "honeypot_id", nullable = false)
    private Honeypot honeypot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attacker_id", nullable = false)
    private Attacker attacker;

    /** Cowrie session identifier, used to correlate log lines into one session. */
    @Column(name = "sensor_session_id", length = 64, unique = true)
    private String sensorSessionId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** ACTIVE or CLOSED. */
    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 20)
    private String protocol;

    @Builder.Default
    @Column(nullable = false)
    private Integer eventCount = 0;

    /** Comma-joined kill-chain stages observed, e.g. "RECONNAISSANCE,ACCESS,EXECUTION". */
    @Column(length = 255)
    private String stages;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Severity maxSeverity;

    /** Pipe-joined commands executed during the session. */
    @Column(length = 1000)
    private String commands;

    @Builder.Default
    @Column(nullable = false)
    private boolean multiStage = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer riskScore = 0;

    /** Short classification, e.g. "Multi-stage SSH intrusion". */
    @Column(length = 80)
    private String attackType;

    @PrePersist
    void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (eventCount == null) {
            eventCount = 0;
        }
        if (riskScore == null) {
            riskScore = 0;
        }
        if (maxSeverity == null) {
            maxSeverity = Severity.INFO;
        }
    }
}
