package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Clave secreta para firmar los tokens (mínimo 32 caracteres)
    private static final String SECRET = "pokeplace-secret-key-2024-segura!!";

    // Tiempo de expiración: 8 horas
    private static final long EXPIRATION = 1000 * 60 * 60 * 8;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Genera un JWT con el email y rol del usuario
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    // Extrae el email del token
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Extrae el rol del token
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // Verifica si el token es válido
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}