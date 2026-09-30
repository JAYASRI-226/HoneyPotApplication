package com.example.honeypot_platform.validation;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Literal IPv4 or IPv6 only — not hostnames.
 * Column length 45 is the MySQL-friendly max for IPv6 text (including IPv4-mapped).
 */
public final class IpAddresses {

    private IpAddresses() {
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String ip = value.trim();
        return isIpv4(ip) || isIpv6(ip);
    }

    public static boolean isIpv4(String ip) {
        String[] parts = ip.split("\\.", -1);
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3) {
                return false;
            }
            for (int i = 0; i < part.length(); i++) {
                if (!Character.isDigit(part.charAt(i))) {
                    return false;
                }
            }
            int n = Integer.parseInt(part);
            if (n < 0 || n > 255) {
                return false;
            }
        }
        return true;
    }

    public static boolean isIpv6(String ip) {
        if (!ip.contains(":") || ip.contains(" ")) {
            return false;
        }
        for (int i = 0; i < ip.length(); i++) {
            char c = ip.charAt(i);
            boolean ok = c == ':' || c == '.' || Character.isDigit(c)
                    || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!ok) {
                return false;
            }
        }
        try {
            return InetAddress.getByName(ip) instanceof Inet6Address;
        } catch (UnknownHostException ex) {
            return false;
        }
    }
}
