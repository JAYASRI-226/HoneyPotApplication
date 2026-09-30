package com.example.honeypot_platform.entity;

public enum UserRole {
    ADMIN,
    ANALYST;

    public static UserRole fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required. Use ADMIN or ANALYST.");
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role '" + value + "'. Use ADMIN or ANALYST.");
        }
    }
}
