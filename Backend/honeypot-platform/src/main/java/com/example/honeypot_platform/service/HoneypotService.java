package com.example.honeypot_platform.service;

import com.example.honeypot_platform.dto.HoneypotCreateRequest;
import com.example.honeypot_platform.dto.HoneypotResponse;
import com.example.honeypot_platform.dto.HoneypotUpdateRequest;
import com.example.honeypot_platform.entity.Honeypot;
import com.example.honeypot_platform.entity.HoneypotStatus;
import com.example.honeypot_platform.entity.HoneypotType;
import com.example.honeypot_platform.exception.InvalidRequestException;
import com.example.honeypot_platform.exception.ResourceNotFoundException;
import com.example.honeypot_platform.repository.HoneypotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HoneypotService {

    private final HoneypotRepository honeypotRepository;

    public HoneypotService(HoneypotRepository honeypotRepository) {
        this.honeypotRepository = honeypotRepository;
    }

    public HoneypotResponse create(HoneypotCreateRequest request) {
        Honeypot honeypot = Honeypot.builder()
                .name(request.name())
                .type(parseType(request.type()))
                .ipAddress(request.ipAddress().trim())
                .status(parseStatus(request.status()))
                .build();
        return HoneypotResponse.from(honeypotRepository.save(honeypot));
    }

    @Transactional(readOnly = true)
    public List<HoneypotResponse> findAll() {
        return honeypotRepository.findAll().stream()
                .map(HoneypotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HoneypotResponse findById(Long id) {
        return HoneypotResponse.from(getOrThrow(id));
    }

    public HoneypotResponse update(Long id, HoneypotUpdateRequest request) {
        Honeypot honeypot = getOrThrow(id);

        if (request.name() != null && !request.name().isBlank()) {
            honeypot.setName(request.name());
        }
        if (request.type() != null && !request.type().isBlank()) {
            honeypot.setType(parseType(request.type()));
        }
        if (request.ipAddress() != null && !request.ipAddress().isBlank()) {
            honeypot.setIpAddress(request.ipAddress().trim());
        }
        if (request.status() != null && !request.status().isBlank()) {
            honeypot.setStatus(parseStatus(request.status()));
        }

        return HoneypotResponse.from(honeypotRepository.save(honeypot));
    }

    public void delete(Long id) {
        if (!honeypotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Honeypot not found with id: " + id);
        }
        honeypotRepository.deleteById(id);
    }

    private Honeypot getOrThrow(Long id) {
        return honeypotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Honeypot not found with id: " + id));
    }

    private HoneypotType parseType(String value) {
        try {
            return HoneypotType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid type '" + value + "'. Use SSH, TELNET, HTTP, or WEB.");
        }
    }

    private HoneypotStatus parseStatus(String value) {
        try {
            return HoneypotStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid status '" + value + "'. Use ACTIVE, INACTIVE, or OFFLINE.");
        }
    }
}
