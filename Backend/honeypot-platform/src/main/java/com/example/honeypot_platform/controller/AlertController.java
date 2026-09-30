package com.example.honeypot_platform.controller;

import com.example.honeypot_platform.dto.AlertResponse;
import com.example.honeypot_platform.entity.Alert;
import com.example.honeypot_platform.service.AlertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(@RequestBody Alert alert) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertService.createAlert(alert));
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<AlertResponse>> getAlertsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(alertService.getAlertsByEvent(eventId));
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<AlertResponse>> getAlertsBySeverity(@PathVariable String severity) {
        return ResponseEntity.ok(alertService.getAlertsBySeverity(severity));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AlertResponse>> getAlertsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(alertService.getAlertsByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertResponse> updateAlert(@PathVariable Long id, @RequestBody Alert alert) {
        return ResponseEntity.ok(alertService.updateAlert(id, alert));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}
