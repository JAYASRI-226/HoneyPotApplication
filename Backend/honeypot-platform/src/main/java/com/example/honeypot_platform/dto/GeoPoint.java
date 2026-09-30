package com.example.honeypot_platform.dto;

/** Aggregated attack origin for geo visualisation. */
public record GeoPoint(
        String label,
        String country,
        String city,
        Double latitude,
        Double longitude,
        long attacks
) {
}
