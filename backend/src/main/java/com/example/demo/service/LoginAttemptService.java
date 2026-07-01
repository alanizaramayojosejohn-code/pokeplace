package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 5;

    private record AttemptData(int count, LocalDateTime lockedUntil) {}

    private final ConcurrentHashMap<String, AttemptData> cache = new ConcurrentHashMap<>();

    public void recordFailure(String email) {
        AttemptData current = cache.getOrDefault(email, new AttemptData(0, null));
        int newCount = current.count() + 1;
        LocalDateTime lockout = newCount >= MAX_ATTEMPTS
                ? LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES)
                : null;
        cache.put(email, new AttemptData(newCount, lockout));
    }

    public void recordSuccess(String email) {
        cache.remove(email);
    }

    public boolean isBlocked(String email) {
        AttemptData data = cache.get(email);
        if (data == null || data.lockedUntil() == null) return false;
        if (LocalDateTime.now().isAfter(data.lockedUntil())) {
            cache.remove(email);
            return false;
        }
        return true;
    }

    public int getRemainingAttempts(String email) {
        AttemptData data = cache.get(email);
        if (data == null) return MAX_ATTEMPTS;
        return Math.max(0, MAX_ATTEMPTS - data.count());
    }

    public long getSecondsUntilUnlock(String email) {
        AttemptData data = cache.get(email);
        if (data == null || data.lockedUntil() == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), data.lockedUntil()).getSeconds();
    }
}
