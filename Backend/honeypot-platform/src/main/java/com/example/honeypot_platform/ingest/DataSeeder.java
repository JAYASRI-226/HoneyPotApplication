package com.example.honeypot_platform.ingest;

import com.example.honeypot_platform.config.HoneypotProperties;
import com.example.honeypot_platform.dto.IngestSummary;
import com.example.honeypot_platform.service.CowrieLogIngester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * On startup: make sure the default sensor exists and ingest the sample Cowrie logs once,
 * so the dashboard shows real, detected attack data immediately.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CowrieLogIngester ingester;
    private final HoneypotProperties properties;

    public DataSeeder(CowrieLogIngester ingester, HoneypotProperties properties) {
        this.ingester = ingester;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        ingester.resolveSensor();

        if (!properties.getIngest().isEnabled()) {
            log.info("Ingestion disabled; skipping startup seed.");
            return;
        }
        if (!properties.getIngest().isSeedOnStartup()) {
            return;
        }

        IngestSummary summary = ingester.ingestAll();
        log.info("Startup ingestion complete: {} new events from {} file(s), {} sessions analysed. {}",
                summary.newEvents(), summary.files(), summary.sessionsAnalyzed(),
                summary.notes().isEmpty() ? "" : "Notes: " + summary.notes());
    }
}
