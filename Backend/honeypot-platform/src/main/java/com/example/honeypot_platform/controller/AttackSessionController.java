package com.example.honeypot_platform.controller;

import com.example.honeypot_platform.dto.AttackSessionDetailResponse;
import com.example.honeypot_platform.dto.AttackSessionResponse;
import com.example.honeypot_platform.entity.AttackSession;
import com.example.honeypot_platform.service.AttackSessionService;
import com.example.honeypot_platform.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attack-sessions")
public class AttackSessionController {

    private final AttackSessionService attackSessionService;
    private final DashboardService dashboardService;

    public AttackSessionController(AttackSessionService attackSessionService,
                                   DashboardService dashboardService) {
        this.attackSessionService = attackSessionService;
        this.dashboardService = dashboardService;
    }

    @PostMapping
    public ResponseEntity<AttackSessionResponse> createSession(@RequestBody AttackSession attackSession) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attackSessionService.createSession(attackSession));
    }

    @GetMapping
    public ResponseEntity<List<AttackSessionResponse>> getAllSessions() {
        return ResponseEntity.ok(attackSessionService.getAllSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttackSessionResponse> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(attackSessionService.getSessionById(id));
    }

    /** Full multi-stage kill-chain view for a single session. */
    @GetMapping("/{id}/chain")
    public ResponseEntity<AttackSessionDetailResponse> getSessionChain(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.sessionDetail(id));
    }

    @GetMapping("/attacker/{attackerId}")
    public ResponseEntity<List<AttackSessionResponse>> getSessionsByAttacker(@PathVariable Long attackerId) {
        return ResponseEntity.ok(attackSessionService.getSessionsByAttacker(attackerId));
    }

    @GetMapping("/honeypot/{honeypotId}")
    public ResponseEntity<List<AttackSessionResponse>> getSessionsByHoneypot(@PathVariable Long honeypotId) {
        return ResponseEntity.ok(attackSessionService.getSessionsByHoneypot(honeypotId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttackSessionResponse> updateSession(@PathVariable Long id, @RequestBody AttackSession attackSession) {
        return ResponseEntity.ok(attackSessionService.updateSession(id, attackSession));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        attackSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
