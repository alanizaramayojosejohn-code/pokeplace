package com.example.demo.service;
import com.example.demo.model.Audit;
import com.example.demo.repository.AuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;        // ← nuevo
import org.springframework.transaction.annotation.Transactional;      // ← nuevo

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;  // ← inyectado por Spring
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void log(String entityType, Long entityId, Audit.AuditAction action,
                    Object previousValue, Object newValue, String performedBy) {
        try {
            Audit audit = Audit.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .previousValue(previousValue != null ? objectMapper.writeValueAsString(previousValue) : null)
                .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .build();

            auditRepository.save(audit);
            log.info("Audit saved: {} {} {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to save audit log for {} {}: {}", entityType, entityId, e.getMessage(), e);
        }
    }
}