package com.example.demo.repository;

import com.example.demo.model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {

    List<Audit> findByEntityTypeOrderByPerformedAtDesc(String entityType);

    List<Audit> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(
        String entityType, Long entityId
    );

    List<Audit> findByPerformedByOrderByPerformedAtDesc(String performedBy);

    List<Audit> findByPerformedAtBetweenOrderByPerformedAtDesc(
        LocalDateTime start, LocalDateTime end
    );

    // ← nueva: por usuario + rango de fechas
    List<Audit> findByPerformedByAndPerformedAtBetweenOrderByPerformedAtDesc(
        String performedBy, LocalDateTime start, LocalDateTime end
    );
}