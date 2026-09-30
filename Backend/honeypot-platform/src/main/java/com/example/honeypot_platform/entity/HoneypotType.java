package com.example.honeypot_platform.entity;

/** Protocol / family of the deployed honeypot. Cowrie is typically SSH or TELNET. */
public enum HoneypotType {
    SSH,
    TELNET,
    HTTP,
    WEB
}
