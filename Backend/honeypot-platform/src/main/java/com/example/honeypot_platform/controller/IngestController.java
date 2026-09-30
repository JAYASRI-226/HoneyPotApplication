package com.example.honeypot_platform.controller;

import com.example.honeypot_platform.config.HoneypotProperties;
import com.example.honeypot_platform.dto.IngestSummary;
import com.example.honeypot_platform.entity.IngestCursor;
import com.example.honeypot_platform.repository.IngestCursorRepository;
import com.example.honeypot_platform.service.CowrieLogIngester;
import tools.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ingestion endpoints: trigger a manual log scan, push live Cowrie events, and inspect cursors.
 */
@RestController
@RequestMapping("/api/ingest")
public class IngestController {

    private final CowrieLogIngester ingester;
    private final IngestCursorRepository cursorRepository;
    private final HoneypotProperties properties;

    public IngestController(CowrieLogIngester ingester,
                            IngestCursorRepository cursorRepository,
                            HoneypotProperties properties) {
        this.ingester = ingester;
        this.cursorRepository = cursorRepository;
        this.properties = properties;
    }

    /** Manually re-scan all configured log files for new lines. */
    @PostMapping("/run")
    public ResponseEntity<IngestSummary> run() {
        return ResponseEntity.ok(ingester.ingestAll());
    }

    /** Accept a single Cowrie event object, or an array of them, pushed live from a sensor. */
    @PostMapping("/cowrie")
    public ResponseEntity<Map<String, Object>> cowrie(@RequestBody JsonNode body) {
        if (body.isArray()) {
            List<Map<String, Object>> results = new ArrayList<>();
            int ingested = 0;
            for (JsonNode node : body) {
                Map<String, Object> r = ingester.ingestExternal(node);
                results.add(r);
                if ("ingested".equals(r.get("status"))) {
                    ingested++;
                }
            }
            return ResponseEntity.ok(Map.of(
                    "received", results.size(),
                    "ingested", ingested,
                    "results", results
            ));
        }
        return ResponseEntity.ok(ingester.ingestExternal(body));
    }

    /** Show configured log paths and how far each has been ingested. */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        List<IngestCursor> cursors = cursorRepository.findAll();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("enabled", properties.getIngest().isEnabled());
        body.put("logPaths", properties.getIngest().getLogPaths());
        body.put("intervalMs", properties.getIngest().getIntervalMs());
        body.put("cursors", cursors);
        return ResponseEntity.ok(body);
    }
}
