package com.example.honeypot_platform.entity;

/**
 * Stages of the honeypot attack kill-chain. Cowrie events are mapped onto these
 * stages so that a session can be analysed as a multi-stage attack.
 *
 * The {@code order} reflects the typical progression of an intrusion; a session
 * that touches several increasing stages is a strong multi-stage signal.
 */
public enum AttackStage {

    UNKNOWN(0, "Unclassified"),
    RECONNAISSANCE(1, "Reconnaissance"),
    ACCESS(2, "Access / Brute-force"),
    EXECUTION(3, "Command Execution"),
    DELIVERY(4, "Payload Delivery"),
    LATERAL_MOVEMENT(5, "Lateral Movement");

    private final int order;
    private final String label;

    AttackStage(int order, String label) {
        this.order = order;
        this.label = label;
    }

    public int getOrder() {
        return order;
    }

    public String getLabel() {
        return label;
    }
}
