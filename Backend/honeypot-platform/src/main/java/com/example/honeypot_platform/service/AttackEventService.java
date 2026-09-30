package com.example.honeypot_platform.service;

import com.example.honeypot_platform.dto.AttackEventResponse;
import com.example.honeypot_platform.entity.AttackEvent;
import com.example.honeypot_platform.exception.ResourceNotFoundException;
import com.example.honeypot_platform.repository.AttackEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AttackEventService {

    private final AttackEventRepository attackEventRepository;

    public AttackEventService(AttackEventRepository attackEventRepository) {
        this.attackEventRepository = attackEventRepository;
    }

    @Transactional
    public AttackEventResponse createEvent(AttackEvent attackEvent) {
        return AttackEventResponse.from(attackEventRepository.save(attackEvent));
    }

    public List<AttackEventResponse> getAllEvents() {
        return attackEventRepository.findAllByOrderByTimestampDesc().stream()
                .map(AttackEventResponse::from)
                .toList();
    }

    public AttackEventResponse getEventById(Long id) {
        return AttackEventResponse.from(getOrThrow(id));
    }

    public List<AttackEventResponse> getEventsBySession(Long sessionId) {
        return attackEventRepository.findBySessionIdOrderByTimestampAsc(sessionId).stream()
                .map(AttackEventResponse::from)
                .toList();
    }

    @Transactional
    public AttackEventResponse updateEvent(Long id, AttackEvent updatedEvent) {
        AttackEvent existing = getOrThrow(id);
        existing.setEventType(updatedEvent.getEventType());
        existing.setEventData(updatedEvent.getEventData());
        existing.setCommand(updatedEvent.getCommand());
        return AttackEventResponse.from(attackEventRepository.save(existing));
    }

    @Transactional
    public void deleteEvent(Long id) {
        attackEventRepository.delete(getOrThrow(id));
    }

    private AttackEvent getOrThrow(Long id) {
        return attackEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attack event not found with id: " + id));
    }
}
