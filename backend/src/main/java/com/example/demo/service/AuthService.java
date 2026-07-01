package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.TooManyAttemptsException;
import com.example.demo.model.Audit;
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
    private final AuditService auditService;
    private final LoginAttemptService loginAttemptService;

    public AuthResponse login(LoginRequest request, String ipAddress) {
        String email = request.getEmail();

        if (loginAttemptService.isBlocked(email)) {
            throw new TooManyAttemptsException(loginAttemptService.getSecondsUntilUnlock(email));
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailure(email);
            int remaining = loginAttemptService.getRemainingAttempts(email);
            if (remaining == 0) {
                throw new TooManyAttemptsException(loginAttemptService.getSecondsUntilUnlock(email));
            }
            throw new InvalidCredentialsException("Invalid credentials. " + remaining + " attempts remaining.");
        }

        loginAttemptService.recordSuccess(email);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        auditService.log("USER", user.getId(), Audit.AuditAction.LOGIN, null, null, user.getEmail(), ipAddress);
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse googleLogin(String googleToken, String ipAddress) {
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
        auditService.log("USER", user.getId(), Audit.AuditAction.LOGIN, null, null, user.getEmail(), ipAddress);
        return new AuthResponse(jwt, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public void logout(String email, String ipAddress) {
        userRepository.findByEmail(email).ifPresent(user ->
            auditService.log("USER", user.getId(), Audit.AuditAction.LOGOUT, null, null, email, ipAddress));
    }
}
