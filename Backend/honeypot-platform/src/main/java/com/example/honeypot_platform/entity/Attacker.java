package com.example.honeypot_platform.entity;

import com.example.honeypot_platform.validation.ValidIpAddress;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A unique attacker identified by source IP address, enriched with geo and risk.
 */
@Entity
@Table(name = "attackers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attacker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ValidIpAddress
    @Column(nullable = false, unique = true, length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime firstSeen;

    @Column(nullable = false)
    private LocalDateTime lastSeen;

    @Column(nullable = false)
    @Builder.Default
    private Long eventCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Integer totalSessions = 0;

    @Column(length = 64)
    private String country;

    @Column(length = 64)
    private String city;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Severity maxSeverity;

    /** 0-100 composite risk score computed by the detection engine. */
    @Builder.Default
    @Column(nullable = false)
    private Integer riskScore = 0;

    @PrePersist
    void onCreate() {
        if (firstSeen == null) {
            firstSeen = LocalDateTime.now();
        }
        if (lastSeen == null) {
            lastSeen = firstSeen;
        }
        if (eventCount == null) {
            eventCount = 0L;
        }
        if (totalSessions == null) {
            totalSessions = 0;
        }
        if (riskScore == null) {
            riskScore = 0;
        }
        if (maxSeverity == null) {
            maxSeverity = Severity.INFO;
        }
    }
}
