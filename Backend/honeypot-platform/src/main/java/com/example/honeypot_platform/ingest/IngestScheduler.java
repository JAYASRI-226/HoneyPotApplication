package com.example.honeypot_platform.ingest;

import com.example.honeypot_platform.config.HoneypotProperties;
import com.example.honeypot_platform.service.CowrieLogIngester;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically tails the configured Cowrie log files for new lines so the dashboard
 * stays live while a honeypot is running.
 */
@Component
public class IngestScheduler {

    private final CowrieLogIngester ingester;
    private final HoneypotProperties properties;

    public IngestScheduler(CowrieLogIngester ingester, HoneypotProperties properties) {
        this.ingester = ingester;
        this.properties = properties;
    }

    @Scheduled(
            fixedDelayString = "${honeypot.ingest.interval-ms:15000}",
            initialDelayString = "${honeypot.ingest.interval-ms:15000}"
    )
    public void pollLogs() {
        if (!properties.getIngest().isEnabled()) {
            return;
        }
        ingester.ingestAll();
    }
}
