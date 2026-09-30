package com.example.honeypot_platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tracks how far each Cowrie log file has been ingested so the scheduled
 * tailer only processes newly appended lines (and never re-ingests on restart).
 */
@Entity
@Table(name = "ingest_cursors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestCursor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String filePath;

    @Builder.Default
    @Column(nullable = false)
    private long linesProcessed = 0L;

    private LocalDateTime lastRunAt;
}
