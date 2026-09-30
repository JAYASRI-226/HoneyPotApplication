package com.example.honeypot_platform.entity;

/**
 * Severity scale shared by events, sessions and alerts.
 * Higher weight == more severe; used to roll a session/attacker maximum.
 */
public enum Severity {

    INFO(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int weight;

    Severity(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public static Severity max(Severity a, Severity b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.weight >= b.weight ? a : b;
    }

    public static Severity fromWeight(int weight) {
        Severity best = INFO;
        for (Severity s : values()) {
            if (s.weight <= weight && s.weight >= best.weight) {
                best = s;
            }
        }
        return best;
    }
}
