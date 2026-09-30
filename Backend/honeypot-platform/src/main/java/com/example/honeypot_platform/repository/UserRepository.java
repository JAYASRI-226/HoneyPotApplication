package com.example.honeypot_platform.repository;

import com.example.honeypot_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository. Hibernate implements this at runtime.
 * You do not write SQL for basic find/save/delete.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
