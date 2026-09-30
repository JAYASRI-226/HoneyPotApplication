package com.example.honeypot_platform.service;

import com.example.honeypot_platform.entity.*;
import com.example.honeypot_platform.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The intelligence core of the platform.
 *
 * <p>It maps raw Cowrie events onto attack kill-chain {@link AttackStage stages}, scores their
 * {@link Severity}, correlates events into multi-stage attack sessions, derives an attacker risk
 * score, and generates security {@link Alert alerts} with a human-readable kill-chain narrative.</p>
 */
@Service
public class AttackDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AttackDetectionService.class);

    private static final Set<String> DANGEROUS_TOKENS = Set.of(
            "wget", "curl", "nc ", "ncat", "netcat", "base64", "chmod +x", "/dev/tcp",
            "sh -c", "bash -i", "rm -rf", "mkfifo", "telnet", "scp", "passwd", "useradd",
            "crontab", "systemctl", "history -c", "eval", "python -c", "perl -e", "tftp"
    );

    private static final Set<String> RECON_COMMAND_TOKENS = Set.of(
            "whoami", "id", "uname", "cat /etc", "ls", "pwd", "hostname", "ifconfig",
            "ip a", "ps ", "netstat", "sudo", "env", "df ", "free", "w ", "last"
    );

    private static final Set<String> NETWORK_TOKENS = Set.of(
            "wget", "curl", "nc ", "ncat", "netcat", "scp", "ssh ", "telnet", "/dev/tcp",
            "ping", "traceroute", "nslookup", "dig ", "ftp"
    );

    private final AttackEventRepository eventRepository;
    private final AttackSessionRepository sessionRepository;
    private final AttackerRepository attackerRepository;
    private final AlertRepository alertRepository;

    public AttackDetectionService(AttackEventRepository eventRepository,
                                  AttackSessionRepository sessionRepository,
                                  AttackerRepository attackerRepository,
                                  AlertRepository alertRepository) {
        this.eventRepository = eventRepository;
        this.sessionRepository = sessionRepository;
        this.attackerRepository = attackerRepository;
        this.alertRepository = alertRepository;
    }

    // ---------------------------------------------------------------------
    //  Classification (pure functions - no DB access)
    // ---------------------------------------------------------------------

    /** Map a Cowrie event (and optional command) onto a kill-chain stage. */
    public AttackStage classifyStage(String eventId, String input) {
        if (eventId == null) {
            return AttackStage.UNKNOWN;
        }
        String e = eventId.toLowerCase();

        if (e.contains("session.connect") || e.contains("client.version")
                || e.contains("client.kex") || e.contains("client.size")
                || e.contains("client.var") || e.contains("session.params")) {
            return AttackStage.RECONNAISSANCE;
        }
        if (e.contains("login")) {
            return AttackStage.ACCESS;
        }
        if (e.contains("command.input")) {
            return classifyCommandStage(input);
        }
        if (e.contains("direct-tcpip") || e.contains("tunnel") || e.contains("proxy")) {
            return AttackStage.LATERAL_MOVEMENT;
        }
        if (e.contains("file") || e.contains("url") || e.contains("download") || e.contains("upload")) {
            return AttackStage.DELIVERY;
        }
        return AttackStage.UNKNOWN;
    }

    private AttackStage classifyCommandStage(String input) {
        if (input == null || input.isBlank()) {
            return AttackStage.EXECUTION;
        }
        String c = input.toLowerCase();
        if (containsAny(c, "| sh", "|sh", "| bash", "|bash", "-o ", "--output", "chmod +x", "base64 -d")) {
            return AttackStage.DELIVERY;
        }
        if (containsAny(c, NETWORK_TOKENS)) {
            return AttackStage.LATERAL_MOVEMENT;
        }
        return AttackStage.EXECUTION;
    }

    /** Score the severity of a single event. */
    public Severity classifySeverity(String eventId, String input) {
        String e = eventId == null ? "" : eventId.toLowerCase();

        if (e.contains("login.success")) {
            return Severity.CRITICAL;
        }
        if (e.contains("login.failed") || e.contains("login.attempt")) {
            return Severity.MEDIUM;
        }
        if (e.contains("command.input")) {
            return commandSeverity(input);
        }
        if (e.contains("file") || e.contains("url") || e.contains("download")) {
            return Severity.HIGH;
        }
        if (e.contains("direct-tcpip") || e.contains("tunnel")) {
            return Severity.HIGH;
        }
        if (e.contains("session.connect")) {
            return Severity.LOW;
        }
        return Severity.INFO;
    }

    private Severity commandSeverity(String input) {
        if (input == null || input.isBlank()) {
            return Severity.MEDIUM;
        }
        String c = input.toLowerCase();
        if (containsAny(c, DANGEROUS_TOKENS)) {
            return Severity.HIGH;
        }
        if (containsAny(c, RECON_COMMAND_TOKENS)) {
            return Severity.MEDIUM;
        }
        return Severity.LOW;
    }

    public static Severity severityFromRisk(int risk) {
        if (risk >= 75) return Severity.CRITICAL;
        if (risk >= 50) return Severity.HIGH;
        if (risk >= 25) return Severity.MEDIUM;
        if (risk > 0) return Severity.LOW;
        return Severity.INFO;
    }

    // ---------------------------------------------------------------------
    //  Session correlation
    // ---------------------------------------------------------------------

    @Transactional
    public void analyzeSessionById(Long sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session ->
                analyzeSession(session, eventRepository.findBySessionIdOrderByTimestampAsc(sessionId)));
    }

    /**
     * Correlate all events of a session into a multi-stage attack profile and regenerate alerts.
     */
    @Transactional
    public void analyzeSession(AttackSession session, List<AttackEvent> events) {
        Set<AttackStage> stageSet = new LinkedHashSet<>();
        Severity maxSev = Severity.INFO;
        List<String> commands = new ArrayList<>();
        boolean loginSuccess = false;
        int failedLogins = 0;
        boolean closed = false;
        LocalDateTime last = session.getEndTime();
        AttackEvent loginEvent = null;
        AttackEvent worstEvent = null;

        for (AttackEvent e : events) {
            if (e.getStage() != null && e.getStage() != AttackStage.UNKNOWN) {
                stageSet.add(e.getStage());
            }
            maxSev = Severity.max(maxSev, e.getSeverity());
            if (worstEvent == null || severityWeight(e) > severityWeight(worstEvent)) {
                worstEvent = e;
            }

            String eid = (e.getCowrieEventId() != null ? e.getCowrieEventId() : e.getEventType());
            if (eid != null) {
                String l = eid.toLowerCase();
                if (l.contains("login.success")) {
                    loginSuccess = true;
                    loginEvent = e;
                }
                if (l.contains("login.failed") || l.contains("login.attempt")) {
                    failedLogins++;
                }
                if (l.contains("session.closed")) {
                    closed = true;
                }
            }
            if (e.getCommand() != null && !e.getCommand().isBlank()) {
                commands.add(e.getCommand());
            }
            if (e.getTimestamp() != null && (last == null || e.getTimestamp().isAfter(last))) {
                last = e.getTimestamp();
            }
        }

        List<AttackStage> orderedStages = stageSet.stream()
                .sorted(Comparator.comparingInt(AttackStage::getOrder))
                .toList();

        boolean recon = orderedStages.contains(AttackStage.RECONNAISSANCE);
        boolean access = orderedStages.contains(AttackStage.ACCESS);
        boolean exec = orderedStages.contains(AttackStage.EXECUTION);
        boolean delivery = orderedStages.contains(AttackStage.DELIVERY);
        boolean lateral = orderedStages.contains(AttackStage.LATERAL_MOVEMENT);

        // A session is "multi-stage" when it progresses through 2+ distinct kill-chain phases
        // and shows real attacker activity (auth or execution), not just a connect + disconnect.
        boolean multiStage = orderedStages.size() >= 2 && (loginSuccess || exec || access);

        List<String> distinctCommands = commands.stream().distinct().toList();

        session.setEventCount(events.size());
        session.setStages(orderedStages.stream().map(Enum::name).collect(Collectors.joining(",")));
        session.setMaxSeverity(maxSev);
        session.setCommands(truncate(String.join(" | ", distinctCommands), 1000));
        session.setMultiStage(multiStage);
        int risk = computeRisk(recon, access, exec, delivery, lateral, loginSuccess, failedLogins, maxSev);
        session.setRiskScore(risk);
        session.setAttackType(classifyAttackType(loginSuccess, multiStage, failedLogins, exec, recon));
        session.setStatus(closed ? "CLOSED" : "ACTIVE");
        if (closed) {
            session.setEndTime(last);
        }
        sessionRepository.save(session);

        regenerateAlerts(session, orderedStages, loginSuccess, failedLogins,
                distinctCommands, risk, loginEvent, worstEvent, events.size());
    }

    private int severityWeight(AttackEvent e) {
        return e.getSeverity() == null ? 0 : e.getSeverity().getWeight();
    }

    private int computeRisk(boolean recon, boolean access, boolean exec, boolean delivery,
                            boolean lateral, boolean loginSuccess, int failedLogins, Severity maxSev) {
        int risk = 0;
        if (recon) risk += 8;
        if (access) risk += 12;
        if (loginSuccess) risk += 28;
        if (exec) risk += 18;
        if (delivery) risk += 15;
        if (lateral) risk += 12;
        risk += Math.min(15, failedLogins * 3);
        risk += (maxSev == null ? 0 : maxSev.getWeight()) * 2;
        return Math.max(0, Math.min(100, risk));
    }

    private String classifyAttackType(boolean loginSuccess, boolean multiStage,
                                      int failedLogins, boolean exec, boolean recon) {
        if (loginSuccess && multiStage) return "Multi-stage SSH intrusion";
        if (loginSuccess) return "Successful login / compromise";
        if (failedLogins >= 3) return "SSH brute-force";
        if (exec) return "Command execution attempt";
        if (recon) return "Reconnaissance scan";
        return "Suspicious activity";
    }

    private void regenerateAlerts(AttackSession session, List<AttackStage> orderedStages,
                                  boolean loginSuccess, int failedLogins, List<String> commands,
                                  int risk, AttackEvent loginEvent, AttackEvent worstEvent, int eventCount) {
        alertRepository.deleteBySessionId(session.getId());

        if (eventCount == 0) {
            return;
        }

        String ip = session.getAttacker() != null ? session.getAttacker().getIpAddress() : "unknown";
        Severity sessionSeverity = severityFromRisk(risk);

        // 1) Successful authentication -> credential compromise (most severe single signal).
        if (loginSuccess) {
            String detail = loginEvent != null && loginEvent.getEventData() != null
                    ? loginEvent.getEventData() : "credentials accepted";
            alertRepository.save(Alert.builder()
                    .session(session)
                    .event(loginEvent)
                    .severity(Severity.CRITICAL.name())
                    .status("NEW")
                    .attackType("Credential compromise")
                    .stage(AttackStage.ACCESS)
                    .sourceIp(ip)
                    .message("Successful SSH login from " + ip)
                    .description("The attacker authenticated successfully (" + detail + "). "
                            + "Treat the honeypot identity as compromised and rotate any shared credentials.")
                    .build());
        }

        // 2) Multi-stage kill chain -> the headline detection for this project.
        if (session.isMultiStage()) {
            String chain = orderedStages.stream()
                    .map(AttackStage::getLabel)
                    .collect(Collectors.joining(" \u2192 "));
            StringBuilder desc = new StringBuilder("Kill chain: ").append(chain).append(". ");
            if (!commands.isEmpty()) {
                desc.append("Commands executed: ").append(String.join(", ", commands)).append(". ");
            }
            if (failedLogins > 0) {
                desc.append(failedLogins).append(" failed login attempt(s) preceded access. ");
            }
            desc.append("Session risk score ").append(risk).append("/100 across ")
                    .append(eventCount).append(" correlated events.");

            AttackStage highest = orderedStages.isEmpty()
                    ? AttackStage.UNKNOWN
                    : orderedStages.get(orderedStages.size() - 1);

            alertRepository.save(Alert.builder()
                    .session(session)
                    .event(worstEvent)
                    .severity(sessionSeverity.name())
                    .status("NEW")
                    .attackType(session.getAttackType())
                    .stage(highest)
                    .sourceIp(ip)
                    .message("Multi-stage attack from " + ip + " across " + orderedStages.size() + " kill-chain phases")
                    .description(desc.toString())
                    .build());
        }

        // 3) Brute force (only when there was no eventual success, to avoid duplicate noise).
        if (!loginSuccess && failedLogins >= 3) {
            alertRepository.save(Alert.builder()
                    .session(session)
                    .severity(Severity.HIGH.name())
                    .status("NEW")
                    .attackType("SSH brute-force")
                    .stage(AttackStage.ACCESS)
                    .sourceIp(ip)
                    .message("SSH brute-force from " + ip)
                    .description(failedLogins + " failed login attempts detected in a single session.")
                    .build());
        }
    }

    // ---------------------------------------------------------------------
    //  Attacker aggregation
    // ---------------------------------------------------------------------

    @Transactional
    public void analyzeAttackerById(Long attackerId) {
        attackerRepository.findById(attackerId).ifPresent(this::analyzeAttacker);
    }

    @Transactional
    public void analyzeAttacker(Attacker attacker) {
        List<AttackSession> sessions = sessionRepository.findByAttackerId(attacker.getId());

        long events = sessions.stream()
                .mapToLong(s -> s.getEventCount() == null ? 0 : s.getEventCount())
                .sum();

        Severity max = Severity.INFO;
        int risk = 0;
        LocalDateTime last = attacker.getLastSeen();

        for (AttackSession s : sessions) {
            max = Severity.max(max, s.getMaxSeverity());
            risk = Math.max(risk, s.getRiskScore() == null ? 0 : s.getRiskScore());
            if (s.getEndTime() != null && (last == null || s.getEndTime().isAfter(last))) {
                last = s.getEndTime();
            }
            if (s.getStartTime() != null && (last == null || s.getStartTime().isAfter(last))) {
                last = s.getStartTime();
            }
        }

        attacker.setTotalSessions(sessions.size());
        attacker.setEventCount(events);
        attacker.setMaxSeverity(max);
        attacker.setRiskScore(risk);
        if (last != null) {
            attacker.setLastSeen(last);
        }
        attackerRepository.save(attacker);
    }

    // ---------------------------------------------------------------------
    //  Geo helpers
    // ---------------------------------------------------------------------

    /** Populate coarse geo for an attacker. Private ranges are labelled as local. */
    public void enrichGeo(Attacker attacker) {
        if (attacker.getCountry() != null || attacker.getIpAddress() == null) {
            return;
        }
        if (isPrivateIp(attacker.getIpAddress())) {
            attacker.setCountry("Private Network");
            attacker.setCity("LAN / Docker bridge");
        } else {
            attacker.setCountry("Unknown");
            attacker.setCity("Unknown");
        }
    }

    public static boolean isPrivateIp(String ip) {
        if (ip == null) {
            return false;
        }
        String v = ip.trim();
        if (v.startsWith("10.") || v.startsWith("127.") || v.startsWith("169.254.")
                || v.equals("::1") || v.toLowerCase().startsWith("fc") || v.toLowerCase().startsWith("fd")) {
            return true;
        }
        if (v.startsWith("192.168.")) {
            return true;
        }
        if (v.startsWith("172.")) {
            String[] parts = v.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------
    //  Utilities
    // ---------------------------------------------------------------------

    private static boolean containsAny(String haystack, Collection<String> needles) {
        for (String n : needles) {
            if (haystack.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
