package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "edibles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Edible extends Product {

    // Nombre creativo del poke bowl
    @Column(nullable = false)
    private String pokeName;
}