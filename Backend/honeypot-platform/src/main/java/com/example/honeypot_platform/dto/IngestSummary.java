package com.example.honeypot_platform.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Aggregated outcome of an ingestion run across all configured log files. */
public record IngestSummary(
        int files,
        int newEvents,
        int duplicates,
        int sessionsAnalyzed,
        List<String> notes,
        LocalDateTime ranAt
) {
}
