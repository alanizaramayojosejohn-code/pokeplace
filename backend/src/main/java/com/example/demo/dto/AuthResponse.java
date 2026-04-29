package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token; // JWT generado por nosotros
    private Long userId;
    private String name;
    private String email;
    private String role;
}