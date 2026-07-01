package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.GoogleTokenVerifier;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse googleLogin(String googleToken) {
        var payload = googleTokenVerifier.verify(googleToken);

        String email = payload.getEmail();
        String googleId = payload.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not registered. Contact your administrator."));

        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
            userRepository.save(user);
        }

        String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(jwt, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
