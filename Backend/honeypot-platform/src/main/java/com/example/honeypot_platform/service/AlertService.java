package com.example.honeypot_platform.service;

import com.example.honeypot_platform.dto.AlertResponse;
import com.example.honeypot_platform.entity.Alert;
import com.example.honeypot_platform.exception.ResourceNotFoundException;
import com.example.honeypot_platform.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional
    public AlertResponse createAlert(Alert alert) {
        return AlertResponse.from(alertRepository.save(alert));
    }

    public List<AlertResponse> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AlertResponse::from)
                .toList();
    }

    public AlertResponse getAlertById(Long id) {
        return AlertResponse.from(getOrThrow(id));
    }

    public List<AlertResponse> getAlertsByEvent(Long eventId) {
        return alertRepository.findByEventId(eventId).stream().map(AlertResponse::from).toList();
    }

    public List<AlertResponse> getAlertsBySeverity(String severity) {
        return alertRepository.findBySeverity(severity).stream().map(AlertResponse::from).toList();
    }

    public List<AlertResponse> getAlertsByStatus(String status) {
        return alertRepository.findByStatus(status).stream().map(AlertResponse::from).toList();
    }

    @Transactional
    public AlertResponse updateAlert(Long id, Alert updatedAlert) {
        Alert existing = getOrThrow(id);
        if (updatedAlert.getSeverity() != null) {
            existing.setSeverity(updatedAlert.getSeverity());
        }
        if (updatedAlert.getMessage() != null) {
            existing.setMessage(updatedAlert.getMessage());
        }
        if (updatedAlert.getStatus() != null) {
            existing.setStatus(updatedAlert.getStatus());
        }
        return AlertResponse.from(alertRepository.save(existing));
    }

    @Transactional
    public void deleteAlert(Long id) {
        alertRepository.delete(getOrThrow(id));
    }

    private Alert getOrThrow(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
    }
}
