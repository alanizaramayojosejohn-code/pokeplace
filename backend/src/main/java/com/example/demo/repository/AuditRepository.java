package com.example.demo.repository;

import com.example.demo.model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditRepository extends JpaRepository<Audit, Long> {

    // Por rango de fechas
    List<Audit> findByEntityTypeAndEntityIdAndPerformedAtBetweenOrderByPerformedAtDesc(
        String entityType, Long entityId, LocalDateTime from, LocalDateTime to
    );

    // Todo el audit por rango de fechas (sin filtrar usuario)
    List<Audit> findByPerformedAtBetweenOrderByPerformedAtDesc(
        LocalDateTime from, LocalDateTime to
    );
    // Audit de una orden específica
List<Audit> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(String entityType, Long entityId);
// Audit de órdenes por usuario (performedBy) y día
@Query("SELECT a FROM Audit a WHERE a.entityType = 'ORDER' " +
       "AND a.performedBy = :username " +
       "AND CAST(a.performedAt AS date) = CAST(:date AS date) " +
       "ORDER BY a.performedAt DESC")
List<Audit> findOrderAuditByUserAndDay(
    @Param("username") String username,
    @Param("date") LocalDateTime date
);
List<Audit> findByEntityTypeOrderByPerformedAtDesc(String entityType);
}