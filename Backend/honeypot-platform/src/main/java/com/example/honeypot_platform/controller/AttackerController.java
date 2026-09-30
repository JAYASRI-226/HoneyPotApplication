package com.example.honeypot_platform.controller;

import com.example.honeypot_platform.entity.Attacker;
import com.example.honeypot_platform.service.AttackerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attackers")
public class AttackerController {

    private final AttackerService attackerService;

    public AttackerController(AttackerService attackerService) {
        this.attackerService = attackerService;
    }

    // Create attacker
    @PostMapping
    public ResponseEntity<Attacker> createAttacker(
            @Valid @RequestBody Attacker attacker) {

        Attacker savedAttacker = attackerService.createAttacker(attacker);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedAttacker);
    }

    // Get all attackers
    @GetMapping
    public ResponseEntity<List<Attacker>> getAllAttackers() {

        return ResponseEntity.ok(
                attackerService.getAllAttackers()
        );
    }

    // Get attacker by ID
    @GetMapping("/{id}")
    public ResponseEntity<Attacker> getAttackerById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                attackerService.getAttackerById(id)
        );
    }

    // Update attacker
    @PutMapping("/{id}")
    public ResponseEntity<Attacker> updateAttacker(
            @PathVariable Long id,
            @Valid @RequestBody Attacker attacker) {

        return ResponseEntity.ok(
                attackerService.updateAttacker(id, attacker)
        );
    }

    // Delete attacker
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttacker(
            @PathVariable Long id) {

        attackerService.deleteAttacker(id);

        return ResponseEntity.noContent().build();
    }
}