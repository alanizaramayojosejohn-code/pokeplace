package com.example.demo.controller;

import com.example.demo.dto.AuditResponseDTO;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<AuditResponseDTO>> getAll() {
        List<AuditResponseDTO> result = new ArrayList<>();

        orderRepository.findAll().forEach(o -> result.add(new AuditResponseDTO(
            o.getId(), "ORDER",
            o.getCreatedBy(), o.getUpdatedBy(),
            o.getCreatedAt(), o.getUpdatedAt()
        )));

        userRepository.findAll().forEach(u -> result.add(new AuditResponseDTO(
            u.getId(), "USER",
            u.getCreatedBy(), u.getUpdatedBy(),
            u.getCreatedAt(), u.getUpdatedAt()
        )));

        result.sort(Comparator.comparing(AuditResponseDTO::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<AuditResponseDTO>> getOrders() {
        List<AuditResponseDTO> result = new ArrayList<>();
        orderRepository.findAll().forEach(o -> result.add(new AuditResponseDTO(
            o.getId(), "ORDER",
            o.getCreatedBy(), o.getUpdatedBy(),
            o.getCreatedAt(), o.getUpdatedAt()
        )));
        result.sort(Comparator.comparing(AuditResponseDTO::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users")
    public ResponseEntity<List<AuditResponseDTO>> getUsers() {
        List<AuditResponseDTO> result = new ArrayList<>();
        userRepository.findAll().forEach(u -> result.add(new AuditResponseDTO(
            u.getId(), "USER",
            u.getCreatedBy(), u.getUpdatedBy(),
            u.getCreatedAt(), u.getUpdatedAt()
        )));
        result.sort(Comparator.comparing(AuditResponseDTO::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-user")
    public ResponseEntity<List<AuditResponseDTO>> getByUser(@RequestParam String username) {
        List<AuditResponseDTO> result = new ArrayList<>();

        orderRepository.findAll().stream()
            .filter(o -> username.equals(o.getCreatedBy()) || username.equals(o.getUpdatedBy()))
            .forEach(o -> result.add(new AuditResponseDTO(
                o.getId(), "ORDER",
                o.getCreatedBy(), o.getUpdatedBy(),
                o.getCreatedAt(), o.getUpdatedAt()
            )));

        userRepository.findAll().stream()
            .filter(u -> username.equals(u.getCreatedBy()) || username.equals(u.getUpdatedBy()))
            .forEach(u -> result.add(new AuditResponseDTO(
                u.getId(), "USER",
                u.getCreatedBy(), u.getUpdatedBy(),
                u.getCreatedAt(), u.getUpdatedAt()
            )));

        result.sort(Comparator.comparing(AuditResponseDTO::getCreatedAt,
            Comparator.nullsLast(Comparator.reverseOrder())));
        return ResponseEntity.ok(result);
    }
}