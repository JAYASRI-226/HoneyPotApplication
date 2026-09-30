package com.example.honeypot_platform.repository;

import com.example.honeypot_platform.entity.AttackSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttackSessionRepository
        extends JpaRepository<AttackSession, Long> {

    List<AttackSession> findByAttackerId(Long attackerId);

    List<AttackSession> findByHoneypotId(Long honeypotId);

    Optional<AttackSession> findBySensorSessionId(String sensorSessionId);

    List<AttackSession> findAllByOrderByStartTimeDesc();

    List<AttackSession> findTop20ByOrderByStartTimeDesc();

    long countByStatus(String status);

    long countByMultiStageTrue();
}
