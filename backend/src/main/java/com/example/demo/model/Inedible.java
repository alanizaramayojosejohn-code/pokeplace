package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inedibles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Inedible extends Product {

    // Stock actual
    @Column(nullable = false)
    private Integer stock;

    // Stock mínimo antes de alertar
    @Column(nullable = false)
    private Integer minStock;
}