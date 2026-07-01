package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String action;

    @Column(nullable = false)
    private String tableName;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    // Relación con User (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;
}