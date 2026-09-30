package com.example.honeypot_platform.controller;

import com.example.honeypot_platform.dto.*;
import com.example.honeypot_platform.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Aggregated, chart-ready endpoints consumed by the React dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public SummaryResponse summary() {
        return dashboardService.summary();
    }

    @GetMapping("/timeline")
    public List<TimelinePoint> timeline() {
        return dashboardService.timeline();
    }

    @GetMapping("/event-types")
    public List<NameCount> eventTypes() {
        return dashboardService.eventTypes();
    }

    @GetMapping("/stages")
    public List<NameCount> stages() {
        return dashboardService.stages();
    }

    @GetMapping("/severity")
    public List<NameCount> severity() {
        return dashboardService.severity();
    }

    @GetMapping("/top-attackers")
    public List<AttackerResponse> topAttackers(@RequestParam(defaultValue = "8") int limit) {
        return dashboardService.topAttackers(limit);
    }

    @GetMapping("/geo")
    public List<GeoPoint> geo() {
        return dashboardService.geo();
    }

    @GetMapping("/recent-events")
    public List<AttackEventResponse> recentEvents(@RequestParam(defaultValue = "20") int limit) {
        return dashboardService.recentEvents(limit);
    }

    @GetMapping("/recent-sessions")
    public List<AttackSessionResponse> recentSessions(@RequestParam(defaultValue = "10") int limit) {
        return dashboardService.recentSessions(limit);
    }
}
