package com.example.demo.service;

import com.example.demo.config.SecurityUtils;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Audit;
import com.example.demo.model.User;
import com.example.demo.repository.AuditRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;       // ← nuevo
    private final SecurityUtils securityUtils;     // ← nuevo
    private final AuditRepository auditRepository;
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);

        // ← auditoría (sin password en el log)
        auditService.log(
            "USER",
            saved.getId(),
            Audit.AuditAction.CREATE,
            null,
            sanitized(saved),
            securityUtils.getCurrentUsername(), null
        );

        return saved;
    }

    @Transactional
    public User updateUser(Long id, User updated) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User previousSnapshot = sanitized(existing); // ← snapshot antes de modificar

        existing.setName(updated.getName());
        existing.setLastname(updated.getLastname());
        existing.setPhone(updated.getPhone());
        existing.setCi(updated.getCi());
        existing.setRole(updated.getRole());
        existing.setSalary(updated.getSalary());

        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }

        User saved = userRepository.save(existing);

        // ← auditoría
        auditService.log(
            "USER",
            id,
            Audit.AuditAction.UPDATE,
            previousSnapshot,
            sanitized(saved),
            securityUtils.getCurrentUsername(), null
        );

        return saved;
    }
    // Por usuario
    public List<Audit> getUserAudit(Long id) {
        return auditRepository.findByEntityTypeAndEntityIdOrderByPerformedAtDesc("USER", id);
    }

    // Por usuario + rango de fechas
    public List<Audit> getUserAuditByDateRange(Long id, LocalDateTime from, LocalDateTime to) {
        return auditRepository.findByEntityTypeAndEntityIdAndPerformedAtBetweenOrderByPerformedAtDesc(
            "USER", id, from, to
        );
    }

    // Todo el audit por rango de fechas
    public List<Audit> getAllAuditByDateRange(LocalDateTime from, LocalDateTime to) {
        return auditRepository.findByPerformedAtBetweenOrderByPerformedAtDesc(from, to);
    }
    @Transactional
    public void deleteUser(Long id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.deleteById(id);

        // ← auditoría
        auditService.log(
            "USER",
            id,
            Audit.AuditAction.DELETE,
            sanitized(existing),
            null,
            securityUtils.getCurrentUsername(), null
        );
    }

    // ← evita guardar el hash del password en los logs
    private User sanitized(User user) {
        User copy = new User();
        copy.setId(user.getId());
        copy.setName(user.getName());
        copy.setLastname(user.getLastname());
        copy.setEmail(user.getEmail());
        copy.setPhone(user.getPhone());
        copy.setCi(user.getCi());
        copy.setRole(user.getRole());
        return copy;
    }
}