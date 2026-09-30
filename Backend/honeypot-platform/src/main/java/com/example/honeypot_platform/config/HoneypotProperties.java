package com.example.honeypot_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds the {@code honeypot.*} settings from application.properties.
 */
@Component
@ConfigurationProperties(prefix = "honeypot")
public class HoneypotProperties {

    private final Ingest ingest = new Ingest();
    private final Sensor sensor = new Sensor();

    public Ingest getIngest() {
        return ingest;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public static class Ingest {
        /** Cowrie JSON-lines log files to ingest. */
        private List<String> logPaths = new ArrayList<>();
        private boolean enabled = true;
        private long intervalMs = 15000;
        private boolean seedOnStartup = true;
        private boolean geoEnrichment = false;

        public List<String> getLogPaths() {
            return logPaths;
        }

        public void setLogPaths(List<String> logPaths) {
            this.logPaths = logPaths;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getIntervalMs() {
            return intervalMs;
        }

        public void setIntervalMs(long intervalMs) {
            this.intervalMs = intervalMs;
        }

        public boolean isSeedOnStartup() {
            return seedOnStartup;
        }

        public void setSeedOnStartup(boolean seedOnStartup) {
            this.seedOnStartup = seedOnStartup;
        }

        public boolean isGeoEnrichment() {
            return geoEnrichment;
        }

        public void setGeoEnrichment(boolean geoEnrichment) {
            this.geoEnrichment = geoEnrichment;
        }
    }

    public static class Sensor {
        private String defaultName = "Cowrie SSH Honeypot";
        private String defaultType = "SSH";
        private String defaultIp = "172.19.0.3";

        public String getDefaultName() {
            return defaultName;
        }

        public void setDefaultName(String defaultName) {
            this.defaultName = defaultName;
        }

        public String getDefaultType() {
            return defaultType;
        }

        public void setDefaultType(String defaultType) {
            this.defaultType = defaultType;
        }

        public String getDefaultIp() {
            return defaultIp;
        }

        public void setDefaultIp(String defaultIp) {
            this.defaultIp = defaultIp;
        }
    }
}
