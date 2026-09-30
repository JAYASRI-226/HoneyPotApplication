package com.example.honeypot_platform.dto;

/** A simple label/count pair used for distribution charts. */
public record NameCount(String name, String label, long count) {

    public static NameCount of(String name, long count) {
        return new NameCount(name, name, count);
    }

    public static NameCount of(String name, String label, long count) {
        return new NameCount(name, label, count);
    }
}
