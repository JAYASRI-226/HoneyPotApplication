package com.example.honeypot_platform.service;

import com.example.honeypot_platform.dto.*;
import com.example.honeypot_platform.entity.*;
import com.example.honeypot_platform.exception.ResourceNotFoundException;
import com.example.honeypot_platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Read-side analytics for the dashboard: summary metrics, distributions,
 * timeline, top attackers, geo and the per-session kill-chain detail.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HoneypotRepository honeypotRepository;
    private final AttackerRepository attackerRepository;
    private final AttackSessionRepository sessionRepository;
    private final AttackEventRepository eventRepository;
    private final AlertRepository alertRepository;

    public DashboardService(HoneypotRepository honeypotRepository,
                            AttackerRepository attackerRepository,
                            AttackSessionRepository sessionRepository,
                            AttackEventRepository eventRepository,
                            AlertRepository alertRepository) {
        this.honeypotRepository = honeypotRepository;
        this.attackerRepository = attackerRepository;
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.alertRepository = alertRepository;
    }

    public SummaryResponse summary() {
        List<NameCount> severityBreakdown = Arrays.stream(Severity.values())
                .map(s -> NameCount.of(s.name(), s.name(), eventRepository.countBySeverity(s)))
                .filter(nc -> nc.count() > 0)
                .toList();

        List<NameCount> stageBreakdown = Arrays.stream(AttackStage.values())
                .filter(st -> st != AttackStage.UNKNOWN)
                .map(st -> NameCount.of(st.name(), st.getLabel(), eventRepository.countByStage(st)))
                .filter(nc -> nc.count() > 0)
                .toList();

        List<AttackSession> sessions = sessionRepository.findAll();
        String topAttackType = sessions.stream()
                .map(AttackSession::getAttackType)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        List<Attacker> attackers = attackerRepository.findAll();
        Attacker top = attackers.stream()
                .max(Comparator.comparingInt(a -> a.getRiskScore() == null ? 0 : a.getRiskScore()))
                .orElse(null);
        int highestRisk = attackers.stream()
                .mapToInt(a -> a.getRiskScore() == null ? 0 : a.getRiskScore())
                .max().orElse(0);

        LocalDateTime lastEventAt = eventRepository.findTop100ByOrderByTimestampDesc().stream()
                .map(AttackEvent::getTimestamp)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new SummaryResponse(
                honeypotRepository.count(),
                honeypotRepository.countByStatus(HoneypotStatus.ACTIVE),
                attackerRepository.count(),
                sessionRepository.count(),
                sessionRepository.countByStatus("ACTIVE"),
                sessionRepository.countByMultiStageTrue(),
                eventRepository.count(),
                alertRepository.count(),
                alertRepository.countByStatus("NEW"),
                alertRepository.countBySeverity("CRITICAL"),
                severityBreakdown,
                stageBreakdown,
                topAttackType,
                top != null ? top.getIpAddress() : null,
                highestRisk,
                lastEventAt
        );
    }

    /** Timeline of events bucketed by hour (or day for wide ranges). */
    public List<TimelinePoint> timeline() {
        List<AttackEvent> events = eventRepository.findAll();
        if (events.isEmpty()) {
            return List.of();
        }
        List<AttackEvent> sorted = events.stream()
                .filter(e -> e.getTimestamp() != null)
                .sorted(Comparator.comparing(AttackEvent::getTimestamp))
                .toList();
        if (sorted.isEmpty()) {
            return List.of();
        }

        LocalDateTime min = sorted.get(0).getTimestamp();
        LocalDateTime max = sorted.get(sorted.size() - 1).getTimestamp();
        boolean hourly = Duration.between(min, max).toHours() <= 72;
        DateTimeFormatter fmt = hourly ? HOUR_FMT : DAY_FMT;

        Map<String, long[]> buckets = new LinkedHashMap<>();
        for (AttackEvent e : sorted) {
            String key = e.getTimestamp().format(fmt);
            long[] counts = buckets.computeIfAbsent(key, k -> new long[2]);
            counts[0]++;
            if (e.getSeverity() == Severity.CRITICAL) {
                counts[1]++;
            }
        }
        return buckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(en -> new TimelinePoint(en.getKey(), en.getValue()[0], en.getValue()[1]))
                .toList();
    }

    public List<NameCount> eventTypes() {
        return eventRepository.countGroupByEventType().stream()
                .map(row -> NameCount.of(String.valueOf(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }

    public List<NameCount> stages() {
        return Arrays.stream(AttackStage.values())
                .map(st -> NameCount.of(st.name(), st.getLabel(), eventRepository.countByStage(st)))
                .filter(nc -> nc.count() > 0)
                .toList();
    }

    public List<NameCount> severity() {
        return Arrays.stream(Severity.values())
                .map(s -> NameCount.of(s.name(), s.name(), eventRepository.countBySeverity(s)))
                .filter(nc -> nc.count() > 0)
                .toList();
    }

    public List<AttackerResponse> topAttackers(int limit) {
        return attackerRepository.findAllByOrderByRiskScoreDesc().stream()
                .limit(Math.max(1, limit))
                .map(AttackerResponse::from)
                .toList();
    }

    public List<GeoPoint> geo() {
        Map<String, GeoAcc> grouped = new LinkedHashMap<>();
        for (Attacker a : attackerRepository.findAll()) {
            String country = a.getCountry() != null ? a.getCountry() : "Unknown";
            String city = a.getCity() != null ? a.getCity() : "Unknown";
            String key = country + "|" + city;
            GeoAcc acc = grouped.computeIfAbsent(key, k -> new GeoAcc(country, city, a.getLatitude(), a.getLongitude()));
            acc.attacks += a.getEventCount() == null ? 0 : a.getEventCount();
        }
        return grouped.values().stream()
                .sorted(Comparator.comparingLong((GeoAcc g) -> g.attacks).reversed())
                .map(g -> new GeoPoint(g.country + (g.city != null ? " - " + g.city : ""),
                        g.country, g.city, g.lat, g.lon, g.attacks))
                .toList();
    }

    public List<AttackEventResponse> recentEvents(int limit) {
        return eventRepository.findTop100ByOrderByTimestampDesc().stream()
                .limit(Math.max(1, limit))
                .map(AttackEventResponse::from)
                .toList();
    }

    public List<AttackSessionResponse> recentSessions(int limit) {
        return sessionRepository.findTop20ByOrderByStartTimeDesc().stream()
                .limit(Math.max(1, limit))
                .map(AttackSessionResponse::from)
                .toList();
    }

    public AttackSessionDetailResponse sessionDetail(Long id) {
        AttackSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attack session not found with id: " + id));

        List<AttackEvent> events = eventRepository.findBySessionIdOrderByTimestampAsc(id);
        List<AttackEventResponse> eventDtos = events.stream().map(AttackEventResponse::from).toList();
        List<AlertResponse> alertDtos = alertRepository.findBySessionId(id).stream()
                .map(AlertResponse::from)
                .toList();

        Map<AttackStage, List<AttackEvent>> byStage = events.stream()
                .filter(e -> e.getStage() != null)
                .collect(Collectors.groupingBy(AttackEvent::getStage));

        List<AttackSessionDetailResponse.StageGroup> chain = byStage.entrySet().stream()
                .sorted(Comparator.comparingInt(en -> en.getKey().getOrder()))
                .map(en -> {
                    List<AttackEventResponse> stageEvents = en.getValue().stream()
                            .map(AttackEventResponse::from)
                            .toList();
                    Severity max = en.getValue().stream()
                            .map(AttackEvent::getSeverity)
                            .filter(Objects::nonNull)
                            .reduce(Severity.INFO, Severity::max);
                    return new AttackSessionDetailResponse.StageGroup(
                            en.getKey().name(), en.getKey().getLabel(), en.getKey().getOrder(),
                            stageEvents.size(), max.name(), stageEvents);
                })
                .toList();

        return new AttackSessionDetailResponse(AttackSessionResponse.from(session), chain, eventDtos, alertDtos);
    }

    private static final class GeoAcc {
        final String country;
        final String city;
        final Double lat;
        final Double lon;
        long attacks;

        GeoAcc(String country, String city, Double lat, Double lon) {
            this.country = country;
            this.city = city;
            this.lat = lat;
            this.lon = lon;
        }
    }
}
