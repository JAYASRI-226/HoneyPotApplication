package com.example.honeypot_platform.repository;

import com.example.honeypot_platform.entity.IngestCursor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngestCursorRepository extends JpaRepository<IngestCursor, Long> {

    Optional<IngestCursor> findByFilePath(String filePath);
}
