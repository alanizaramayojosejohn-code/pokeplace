package com.example.demo.controller;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.Audit;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(
                userService.getAllUsers().stream().map(UserResponse::from).toList()
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(UserResponse.from(userService.createUser(request.toEntity())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(UserResponse.from(userService.updateUser(id, request.toEntity())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    // Historial completo de un usuario
    @GetMapping("/{id}/audit")
    public ResponseEntity<List<Audit>> getUserAudit(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserAudit(id));
    }

    // Historial de un usuario filtrado por fechas
    // Ejemplo: /users/1/audit/range?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
    @GetMapping("/{id}/audit/range")
    public ResponseEntity<List<Audit>> getUserAuditByDateRange(
            @PathVariable Long id,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        return ResponseEntity.ok(userService.getUserAuditByDateRange(id, from, to));
    }

    // Todo el audit por rango de fechas (todos los usuarios)
    // Ejemplo: /users/audit/range?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
    @GetMapping("/audit/range")
    public ResponseEntity<List<Audit>> getAllAuditByDateRange(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        return ResponseEntity.ok(userService.getAllAuditByDateRange(from, to));
    }
}
