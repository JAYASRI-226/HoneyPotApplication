package com.example.honeypot_platform.dto;

/** Outcome of ingesting a single Cowrie log file. */
public record IngestResult(
        String path,
        int newEvents,
        int duplicates,
        int sessionsAnalyzed,
        String note
) {
    public static IngestResult notFound(String path) {
        return new IngestResult(path, 0, 0, 0, "file not found");
    }

    public static IngestResult error(String path, String message) {
        return new IngestResult(path, 0, 0, 0, message);
    }
}
