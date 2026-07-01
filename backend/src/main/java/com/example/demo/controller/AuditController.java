package com.example.demo.controller;

import com.example.demo.dto.AuditResponseDTO;
import com.example.demo.model.Audit;
import com.example.demo.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditRepository auditRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Specification<Audit> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (entityType != null && !entityType.isBlank())
                predicates.add(cb.equal(root.get("entityType"), entityType));

            if (action != null && !action.isBlank())
                predicates.add(cb.equal(root.get("action"), Audit.AuditAction.valueOf(action)));

            if (performedBy != null && !performedBy.isBlank())
                predicates.add(cb.like(cb.lower(root.get("performedBy")), "%" + performedBy.toLowerCase() + "%"));

            if (startDate != null && !startDate.isBlank())
                predicates.add(cb.greaterThanOrEqualTo(root.get("performedAt"),
                        LocalDate.parse(startDate).atStartOfDay()));

            if (endDate != null && !endDate.isBlank())
                predicates.add(cb.lessThanOrEqualTo(root.get("performedAt"),
                        LocalDate.parse(endDate).atTime(LocalTime.MAX)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var pageable = PageRequest.of(page, size, Sort.by("performedAt").descending());
        var result = auditRepository.findAll(spec, pageable);

        List<AuditResponseDTO> content = result.getContent().stream().map(this::toDTO).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());

        return ResponseEntity.ok(response);
    }

    private AuditResponseDTO toDTO(Audit a) {
        return new AuditResponseDTO(
            a.getId(),
            a.getEntityType(),
            a.getEntityId(),
            a.getAction().name(),
            a.getPreviousValue(),
            a.getNewValue(),
            a.getPerformedBy(),
            a.getPerformedAt(),
            a.getIpAddress()
        );
    }
}
