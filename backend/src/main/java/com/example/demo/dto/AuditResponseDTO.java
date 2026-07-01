package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuditResponseDTO {
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private String previousValue;
    private String newValue;
    private String performedBy;
    private LocalDateTime performedAt;
    private String ipAddress;
}
