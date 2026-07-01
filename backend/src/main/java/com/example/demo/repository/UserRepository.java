package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Busca un usuario por email (para el login)
    Optional<User> findByEmail(String email);

    // Busca un usuario por su ID de Google (para login con Google)
    Optional<User> findByGoogleId(String googleId);

    // Verifica si ya existe un email registrado
    boolean existsByEmail(String email);
}