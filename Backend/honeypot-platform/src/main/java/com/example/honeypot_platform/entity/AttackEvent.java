package com.example.honeypot_platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One activity recorded during an attack session (a parsed Cowrie log line).
 */
@Entity
@Table(name = "attack_events", indexes = {
        @Index(name = "idx_event_stage", columnList = "stage"),
        @Index(name = "idx_event_timestamp", columnList = "event_timestamp"),
        @Index(name = "idx_event_session", columnList = "session_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttackEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AttackSession session;

    /** Cowrie eventid, e.g. {@code cowrie.command.input}. */
    @Column(nullable = false, length = 60)
    private String eventType;

    @Column(length = 60)
    private String cowrieEventId;

    /** Human-readable message or raw payload for the event. */
    @Column(columnDefinition = "TEXT")
    private String eventData;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 45)
    private String sourceIp;

    @Column(length = 45)
    private String destinationIp;

    private Integer destinationPort;

    @Column(length = 20)
    private String protocol;

    /** Command/input captured for command events. */
    @Column(length = 1000)
    private String command;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AttackStage stage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Severity severity;

    /** Stable dedupe key so re-ingesting the same log line never duplicates. */
    @Column(length = 90, unique = true)
    private String fingerprint;

    @PrePersist
    void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (stage == null) {
            stage = AttackStage.UNKNOWN;
        }
        if (severity == null) {
            severity = Severity.INFO;
        }
    }
}
