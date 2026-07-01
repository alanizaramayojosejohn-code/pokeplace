package com.example.demo.controller;

import com.example.demo.model.Audit;
import com.example.demo.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<Audit>> getAll() {
        return ResponseEntity.ok(auditService.getAll());
    }

    @GetMapping("/table/{tableName}")
    public ResponseEntity<List<Audit>> getByTable(@PathVariable String tableName) {
        return ResponseEntity.ok(auditService.getByTable(tableName));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Audit>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditService.getByUser(userId));
    }
}