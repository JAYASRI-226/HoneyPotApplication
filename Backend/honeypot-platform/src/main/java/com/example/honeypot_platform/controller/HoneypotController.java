package com.example.honeypot_platform.controller;

import com.example.honeypot_platform.dto.HoneypotCreateRequest;
import com.example.honeypot_platform.dto.HoneypotResponse;
import com.example.honeypot_platform.dto.HoneypotUpdateRequest;
import com.example.honeypot_platform.service.HoneypotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/honeypots")
public class HoneypotController {

    private final HoneypotService honeypotService;

    public HoneypotController(HoneypotService honeypotService) {
        this.honeypotService = honeypotService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HoneypotResponse> create(@Valid @RequestBody HoneypotCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(honeypotService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<HoneypotResponse>> findAll() {
        return ResponseEntity.ok(honeypotService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoneypotResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(honeypotService.findById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HoneypotResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody HoneypotUpdateRequest request) {
        return ResponseEntity.ok(honeypotService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        honeypotService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
