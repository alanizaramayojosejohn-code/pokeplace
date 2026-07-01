package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Audit;
import com.example.demo.model.User;
import com.example.demo.repository.AuditRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;

    public List<Audit> getAll() {
        return auditRepository.findAll();
    }

    public List<Audit> getByTable(String tableName) {
        return auditRepository.findByTableName(tableName);
    }

    public List<Audit> getByUser(Long userId) {
        return auditRepository.findByUserId(userId);
    }

    public void log(String action, String tableName, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Audit audit = Audit.builder()
                .action(action)
                .tableName(tableName)
                .dateTime(LocalDateTime.now())
                .user(user)
                .build();

        auditRepository.save(audit);
    }
}
