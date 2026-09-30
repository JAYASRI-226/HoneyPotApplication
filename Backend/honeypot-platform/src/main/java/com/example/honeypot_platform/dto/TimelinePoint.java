package com.example.honeypot_platform.dto;

/** One point on the attack timeline. {@code bucket} is an ISO date or date-hour string. */
public record TimelinePoint(String bucket, long count, long critical) {
}
