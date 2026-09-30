package com.example.honeypot_platform.controller;

import com.example.honeypot_platform.dto.AttackEventResponse;
import com.example.honeypot_platform.entity.AttackEvent;
import com.example.honeypot_platform.service.AttackEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attack-events")
public class AttackEventController {

    private final AttackEventService attackEventService;

    public AttackEventController(AttackEventService attackEventService) {
        this.attackEventService = attackEventService;
    }

    @PostMapping
    public ResponseEntity<AttackEventResponse> createEvent(@RequestBody AttackEvent attackEvent) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attackEventService.createEvent(attackEvent));
    }

    @GetMapping
    public ResponseEntity<List<AttackEventResponse>> getAllEvents() {
        return ResponseEntity.ok(attackEventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttackEventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(attackEventService.getEventById(id));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AttackEventResponse>> getEventsBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attackEventService.getEventsBySession(sessionId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttackEventResponse> updateEvent(@PathVariable Long id, @RequestBody AttackEvent attackEvent) {
        return ResponseEntity.ok(attackEventService.updateEvent(id, attackEvent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        attackEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
