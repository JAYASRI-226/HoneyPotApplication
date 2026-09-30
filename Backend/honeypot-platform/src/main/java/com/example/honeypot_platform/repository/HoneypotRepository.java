package com.example.honeypot_platform.repository;

import com.example.honeypot_platform.entity.Honeypot;
import com.example.honeypot_platform.entity.HoneypotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HoneypotRepository extends JpaRepository<Honeypot, Long> {

    Optional<Honeypot> findFirstByName(String name);

    long countByStatus(HoneypotStatus status);
}
