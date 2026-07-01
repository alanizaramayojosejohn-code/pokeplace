package com.example.demo.service;
import com.example.demo.model.Audit;
import com.example.demo.repository.AuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;  // ← inyectado por Spring
    public void log(String entityType, Long entityId, Audit.AuditAction action,
                    Object previousValue, Object newValue, String performedBy) {
        log(entityType, entityId, action, previousValue, newValue, performedBy, null);
    }

    public void log(String entityType, Long entityId, Audit.AuditAction action,
                    Object previousValue, Object newValue, String performedBy, String ipAddress) {
        try {
            Audit audit = Audit.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .previousValue(previousValue != null ? objectMapper.writeValueAsString(previousValue) : null)
                .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build();

            auditRepository.save(audit);
            log.info("Audit saved: {} {} {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to save audit log for {} {}: {}", entityType, entityId, e.getMessage(), e);
        }
    }
}