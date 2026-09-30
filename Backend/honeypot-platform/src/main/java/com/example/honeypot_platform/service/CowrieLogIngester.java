package com.example.honeypot_platform.service;

import com.example.honeypot_platform.config.HoneypotProperties;
import com.example.honeypot_platform.dto.IngestResult;
import com.example.honeypot_platform.dto.IngestSummary;
import com.example.honeypot_platform.entity.*;
import com.example.honeypot_platform.repository.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Ingests Cowrie JSON-lines logs into the platform.
 *
 * <p>Each line becomes an {@link AttackEvent}, correlated into an {@link AttackSession} (by the
 * Cowrie session id) and an {@link Attacker} (by source IP). A per-file cursor makes ingestion
 * incremental and idempotent, so the scheduler can tail growing logs and restarts never duplicate.</p>
 */
@Service
public class CowrieLogIngester {

    private static final Logger log = LoggerFactory.getLogger(CowrieLogIngester.class);

    private final ObjectMapper objectMapper;
    private final HoneypotProperties properties;
    private final AttackDetectionService detection;

    private final HoneypotRepository honeypotRepository;
    private final AttackerRepository attackerRepository;
    private final AttackSessionRepository sessionRepository;
    private final AttackEventRepository eventRepository;
    private final IngestCursorRepository cursorRepository;

    public CowrieLogIngester(ObjectMapper objectMapper,
                             HoneypotProperties properties,
                             AttackDetectionService detection,
                             HoneypotRepository honeypotRepository,
                             AttackerRepository attackerRepository,
                             AttackSessionRepository sessionRepository,
                             AttackEventRepository eventRepository,
                             IngestCursorRepository cursorRepository) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.detection = detection;
        this.honeypotRepository = honeypotRepository;
        this.attackerRepository = attackerRepository;
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.cursorRepository = cursorRepository;
    }

    private record Processed(Long sessionId, Long attackerId) {
    }

    /** Ingest every configured log file. Safe to call repeatedly (incremental). */
    @Transactional
    public IngestSummary ingestAll() {
        int files = 0, newEvents = 0, duplicates = 0, sessions = 0;
        List<String> notes = new ArrayList<>();

        for (String raw : properties.getIngest().getLogPaths()) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            IngestResult r = ingestFile(raw.trim());
            files++;
            newEvents += r.newEvents();
            duplicates += r.duplicates();
            sessions += r.sessionsAnalyzed();
            if (r.note() != null && !r.note().isBlank()) {
                notes.add(Paths.get(r.path()).getFileName() + ": " + r.note());
            }
        }
        return new IngestSummary(files, newEvents, duplicates, sessions, notes, LocalDateTime.now());
    }

    /** Ingest newly appended lines from one file, then re-analyse affected sessions/attackers. */
    @Transactional
    public IngestResult ingestFile(String rawPath) {
        Path path;
        try {
            path = Paths.get(rawPath);
        } catch (Exception ex) {
            return IngestResult.error(rawPath, "invalid path");
        }
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return IngestResult.notFound(rawPath);
        }

        String absolute = path.toAbsolutePath().toString();
        IngestCursor cursor = cursorRepository.findByFilePath(absolute)
                .orElseGet(() -> IngestCursor.builder()
                        .filePath(absolute)
                        .linesProcessed(0L)
                        .build());

        long skip = cursor.getLinesProcessed();
        long lineNo = 0;
        int newEvents = 0;
        int duplicates = 0;
        Set<Long> affectedSessions = new LinkedHashSet<>();
        Set<Long> affectedAttackers = new LinkedHashSet<>();

        Honeypot sensor = resolveSensor();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (lineNo <= skip || line.isBlank()) {
                    continue;
                }
                try {
                    JsonNode node = objectMapper.readTree(line);
                    Processed p = processEvent(node, sensor);
                    if (p == null) {
                        duplicates++;
                    } else {
                        newEvents++;
                        affectedSessions.add(p.sessionId());
                        affectedAttackers.add(p.attackerId());
                    }
                } catch (Exception ex) {
                    log.warn("Skipping unparseable log line {} in {}: {}", lineNo, absolute, ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error("Failed to read log file {}: {}", absolute, ex.getMessage());
            return IngestResult.error(rawPath, ex.getMessage());
        }

        cursor.setLinesProcessed(lineNo);
        cursor.setLastRunAt(LocalDateTime.now());
        cursorRepository.save(cursor);

        for (Long sid : affectedSessions) {
            detection.analyzeSessionById(sid);
        }
        for (Long aid : affectedAttackers) {
            detection.analyzeAttackerById(aid);
        }

        if (newEvents > 0) {
            log.info("Ingested {} new events from {} ({} sessions, {} duplicates skipped)",
                    newEvents, path.getFileName(), affectedSessions.size(), duplicates);
        }

        return new IngestResult(absolute, newEvents, duplicates, affectedSessions.size(), null);
    }

    /** Ingest a single externally supplied Cowrie-style event (live push endpoint). */
    @Transactional
    public Map<String, Object> ingestExternal(JsonNode node) {
        Honeypot sensor = resolveSensor();
        Processed p = processEvent(node, sensor);
        if (p == null) {
            return Map.of("status", "duplicate");
        }
        detection.analyzeSessionById(p.sessionId());
        detection.analyzeAttackerById(p.attackerId());
        return Map.of(
                "status", "ingested",
                "sessionId", p.sessionId(),
                "attackerId", p.attackerId()
        );
    }

    // ---------------------------------------------------------------------

    private Processed processEvent(JsonNode node, Honeypot sensor) {
        String srcIp = text(node, "src_ip");
        String eventId = text(node, "eventid");
        if (srcIp == null || srcIp.isBlank()) {
            return null; // nothing to attribute the event to
        }
        if (eventId == null || eventId.isBlank()) {
            eventId = "cowrie.unknown";
        }

        String cowrieSession = text(node, "session");
        String timestampStr = text(node, "timestamp");
        String input = text(node, "input");
        String username = text(node, "username");
        Integer srcPort = node.hasNonNull("src_port") ? node.get("src_port").asInt() : null;

        String fingerprint = fingerprint(cowrieSession, eventId, timestampStr, srcPort, input, username);
        if (eventRepository.existsByFingerprint(fingerprint)) {
            return null;
        }

        LocalDateTime ts = parseTimestamp(timestampStr);
        String protocol = text(node, "protocol");

        Attacker attacker = attackerRepository.findByIpAddress(srcIp).orElse(null);
        if (attacker == null) {
            attacker = Attacker.builder()
                    .ipAddress(srcIp)
                    .firstSeen(ts)
                    .lastSeen(ts)
                    .eventCount(0L)
                    .totalSessions(0)
                    .riskScore(0)
                    .maxSeverity(Severity.INFO)
                    .build();
            detection.enrichGeo(attacker);
            attacker = attackerRepository.save(attacker);
        } else {
            if (ts.isBefore(attacker.getFirstSeen())) {
                attacker.setFirstSeen(ts);
            }
            if (ts.isAfter(attacker.getLastSeen())) {
                attacker.setLastSeen(ts);
            }
            if (attacker.getCountry() == null) {
                detection.enrichGeo(attacker);
            }
            attacker = attackerRepository.save(attacker);
        }

        AttackSession session = (cowrieSession == null || cowrieSession.isBlank())
                ? null
                : sessionRepository.findBySensorSessionId(cowrieSession).orElse(null);

        if (session == null) {
            session = AttackSession.builder()
                    .honeypot(sensor)
                    .attacker(attacker)
                    .sensorSessionId(cowrieSession)
                    .startTime(ts)
                    .status("ACTIVE")
                    .protocol(protocol)
                    .eventCount(0)
                    .riskScore(0)
                    .maxSeverity(Severity.INFO)
                    .multiStage(false)
                    .build();
            session = sessionRepository.save(session);
        } else {
            if (ts.isBefore(session.getStartTime())) {
                session.setStartTime(ts);
            }
            if (session.getProtocol() == null) {
                session.setProtocol(protocol);
            }
        }

        AttackEvent event = AttackEvent.builder()
                .session(session)
                .eventType(truncate(eventId, 60))
                .cowrieEventId(truncate(eventId, 60))
                .eventData(text(node, "message"))
                .timestamp(ts)
                .sourceIp(srcIp)
                .destinationIp(text(node, "dst_ip"))
                .destinationPort(node.hasNonNull("dst_port") ? node.get("dst_port").asInt() : null)
                .protocol(protocol)
                .command(truncate(input, 1000))
                .stage(detection.classifyStage(eventId, input))
                .severity(detection.classifySeverity(eventId, input))
                .fingerprint(fingerprint)
                .build();
        eventRepository.save(event);

        return new Processed(session.getId(), attacker.getId());
    }

    /** Find (or create) the honeypot sensor that these logs belong to. */
    @Transactional
    public Honeypot resolveSensor() {
        String name = properties.getSensor().getDefaultName();
        return honeypotRepository.findFirstByName(name).orElseGet(() -> {
            HoneypotType type;
            try {
                type = HoneypotType.valueOf(properties.getSensor().getDefaultType().trim().toUpperCase());
            } catch (Exception ex) {
                type = HoneypotType.SSH;
            }
            Honeypot honeypot = Honeypot.builder()
                    .name(name)
                    .type(type)
                    .ipAddress(properties.getSensor().getDefaultIp())
                    .status(HoneypotStatus.ACTIVE)
                    .build();
            return honeypotRepository.save(honeypot);
        });
    }

    private LocalDateTime parseTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
        } catch (Exception ex) {
            try {
                return LocalDateTime.parse(value);
            } catch (Exception ignored) {
                return LocalDateTime.now();
            }
        }
    }

    private String fingerprint(String session, String eventId, String timestamp,
                               Integer srcPort, String input, String username) {
        String key = String.join("|",
                nullSafe(session), nullSafe(eventId), nullSafe(timestamp),
                srcPort == null ? "" : srcPort.toString(),
                nullSafe(input), nullSafe(username));
        return sha256(key);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.substring(0, 64);
        } catch (Exception ex) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private static String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
