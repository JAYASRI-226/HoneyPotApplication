package com.example.honeypot_platform.service;

import com.example.honeypot_platform.dto.AttackSessionResponse;
import com.example.honeypot_platform.entity.AttackSession;
import com.example.honeypot_platform.exception.ResourceNotFoundException;
import com.example.honeypot_platform.repository.AttackSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AttackSessionService {

    private final AttackSessionRepository attackSessionRepository;

    public AttackSessionService(AttackSessionRepository attackSessionRepository) {
        this.attackSessionRepository = attackSessionRepository;
    }

    @Transactional
    public AttackSessionResponse createSession(AttackSession attackSession) {
        return AttackSessionResponse.from(attackSessionRepository.save(attackSession));
    }

    public List<AttackSessionResponse> getAllSessions() {
        return attackSessionRepository.findAllByOrderByStartTimeDesc().stream()
                .map(AttackSessionResponse::from)
                .toList();
    }

    public AttackSessionResponse getSessionById(Long id) {
        return AttackSessionResponse.from(getOrThrow(id));
    }

    public List<AttackSessionResponse> getSessionsByAttacker(Long attackerId) {
        return attackSessionRepository.findByAttackerId(attackerId).stream()
                .map(AttackSessionResponse::from)
                .toList();
    }

    public List<AttackSessionResponse> getSessionsByHoneypot(Long honeypotId) {
        return attackSessionRepository.findByHoneypotId(honeypotId).stream()
                .map(AttackSessionResponse::from)
                .toList();
    }

    @Transactional
    public AttackSessionResponse updateSession(Long id, AttackSession updatedSession) {
        AttackSession existing = getOrThrow(id);
        if (updatedSession.getEndTime() != null) {
            existing.setEndTime(updatedSession.getEndTime());
        }
        if (updatedSession.getStatus() != null) {
            existing.setStatus(updatedSession.getStatus());
        }
        return AttackSessionResponse.from(attackSessionRepository.save(existing));
    }

    @Transactional
    public void deleteSession(Long id) {
        attackSessionRepository.delete(getOrThrow(id));
    }

    private AttackSession getOrThrow(Long id) {
        return attackSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attack session not found with id: " + id));
    }
}
