package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String lastname;
    
    private String phone;

    @Column(unique = true)
    private Integer ci;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String googleId;

    // Sueldo quincenal del empleado
    private Double salary;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ADMIN, CASHIER, KITCHEN
    }
}