package com.example.demo.controller;

import com.example.demo.model.Audit;
import com.example.demo.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditRepository auditRepository;

    @GetMapping("/orders")
    public ResponseEntity<List<Audit>> getOrderAudits() {
        return ResponseEntity.ok(
            auditRepository.findByEntityTypeOrderByPerformedAtDesc("ORDER")
        );
    }

    @GetMapping("/users")
    public ResponseEntity<List<Audit>> getUserAudits() {
        return ResponseEntity.ok(
            auditRepository.findByEntityTypeOrderByPerformedAtDesc("USER")
        );
    }
}