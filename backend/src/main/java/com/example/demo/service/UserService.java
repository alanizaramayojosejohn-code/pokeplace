package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        // Encripta la contraseña antes de guardar
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    public User updateUser(Long id, User updated) {
    User existing = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

    existing.setName(updated.getName());
    existing.setLastname(updated.getLastname());
    existing.setPhone(updated.getPhone());
    existing.setCi(updated.getCi());
    existing.setRole(updated.getRole());

    // Solo actualiza la contraseña si viene una nueva
    if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
        existing.setPassword(passwordEncoder.encode(updated.getPassword()));
    }

    return userRepository.save(existing);
}

 
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}