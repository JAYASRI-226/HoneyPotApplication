package com.example.honeypot_platform.repository;

import com.example.honeypot_platform.entity.AttackEvent;
import com.example.honeypot_platform.entity.AttackStage;
import com.example.honeypot_platform.entity.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttackEventRepository
        extends JpaRepository<AttackEvent, Long> {

    List<AttackEvent> findBySessionId(Long sessionId);

    List<AttackEvent> findBySessionIdOrderByTimestampAsc(Long sessionId);

    boolean existsByFingerprint(String fingerprint);

    List<AttackEvent> findAllByOrderByTimestampDesc();

    List<AttackEvent> findTop100ByOrderByTimestampDesc();

    long countByStage(AttackStage stage);

    long countBySeverity(Severity severity);

    /** Event counts grouped by Cowrie event type, most frequent first. */
    @Query("select e.eventType as name, count(e) as cnt from AttackEvent e "
            + "group by e.eventType order by count(e) desc")
    List<Object[]> countGroupByEventType();
}
