package com.example.demo.service;

import com.example.demo.model.Audit;
import com.example.demo.repository.AuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public void log(String entityType,
                    Long entityId,
                    Audit.AuditAction action,
                    Object previousValue,
                    Object newValue,
                    String performedBy,
                    HttpServletRequest request) {
        try {
            Audit audit = Audit.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .previousValue(previousValue != null ? objectMapper.writeValueAsString(previousValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .performedBy(performedBy)
                    .performedAt(LocalDateTime.now())
                    .ipAddress(extractIp(request))
                    .build();

            auditRepository.save(audit);
        } catch (Exception e) {
            // log error pero no interrumpir el flujo
        }
    }

    private String extractIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isBlank()) ? ip.split(",")[0].trim() : request.getRemoteAddr();
    }
}