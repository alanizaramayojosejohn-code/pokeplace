package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.GoogleLoginRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final HttpServletRequest httpServletRequest;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request, httpServletRequest.getRemoteAddr()));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request.getToken(), httpServletRequest.getRemoteAddr()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String email) {
        authService.logout(email, httpServletRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
