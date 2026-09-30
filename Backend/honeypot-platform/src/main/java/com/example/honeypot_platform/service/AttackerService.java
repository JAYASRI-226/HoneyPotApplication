package com.example.honeypot_platform.service;

import com.example.honeypot_platform.entity.Attacker;
import com.example.honeypot_platform.repository.AttackerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttackerService {

    private final AttackerRepository attackerRepository;

    public AttackerService(AttackerRepository attackerRepository) {
        this.attackerRepository = attackerRepository;
    }

    // Create attacker
    public Attacker createAttacker(Attacker attacker) {

        if (attackerRepository.existsByIpAddress(attacker.getIpAddress())) {
            throw new IllegalArgumentException("Attacker with this IP already exists");
        }

        return attackerRepository.save(attacker);
    }

    // Get all attackers
    public List<Attacker> getAllAttackers() {
        return attackerRepository.findAll();
    }

    // Get attacker by ID
    public Attacker getAttackerById(Long id) {

        return attackerRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Attacker not found with id: " + id)
                );
    }

    // Update attacker
    public Attacker updateAttacker(Long id, Attacker updatedAttacker) {

        Attacker existingAttacker = getAttackerById(id);

        existingAttacker.setIpAddress(updatedAttacker.getIpAddress());
        existingAttacker.setEventCount(updatedAttacker.getEventCount());

        return attackerRepository.save(existingAttacker);
    }

    // Delete attacker
    public void deleteAttacker(Long id) {

        Attacker attacker = getAttackerById(id);

        attackerRepository.delete(attacker);
    }
}