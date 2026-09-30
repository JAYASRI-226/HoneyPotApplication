package com.example.honeypot_platform.repository;

import com.example.honeypot_platform.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository
        extends JpaRepository<Alert, Long> {

    List<Alert> findByEventId(Long eventId);

    List<Alert> findBySeverity(String severity);

    List<Alert> findByStatus(String status);

    List<Alert> findBySessionId(Long sessionId);

    List<Alert> findAllByOrderByCreatedAtDesc();

    List<Alert> findTop20ByOrderByCreatedAtDesc();

    long countByStatus(String status);

    long countBySeverity(String severity);

    void deleteBySessionId(Long sessionId);
}
