package com.example.honeypot_platform.repository;

import com.example.honeypot_platform.entity.Attacker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttackerRepository extends JpaRepository<Attacker, Long> {

    Optional<Attacker> findByIpAddress(String ipAddress);

    boolean existsByIpAddress(String ipAddress);

    List<Attacker> findAllByOrderByRiskScoreDesc();

    List<Attacker> findAllByOrderByLastSeenDesc();

    List<Attacker> findTop10ByOrderByRiskScoreDesc();
}
