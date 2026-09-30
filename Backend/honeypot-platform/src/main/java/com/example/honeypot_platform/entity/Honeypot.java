package com.example.honeypot_platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A deployed honeypot sensor. Hibernate creates the {@code honeypots} table from this class.
 * IP is stored as text so both IPv4 and IPv6 fit (do not use an INT IPv4-only column).
 */
@Entity
@Table(name = "honeypots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Honeypot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HoneypotType type;

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HoneypotStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
