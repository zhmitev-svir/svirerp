package com.svivanrilski.svirerp.auth;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory brute-force lockout for the break-glass local admin login.
 * Resets on app restart — acceptable for a single-instance app, and avoids
 * needing a new persistent table for a single account.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private record Attempt(int count, Instant windowStart) {
    }

    private final ConcurrentHashMap<String, Attempt> attemptsByKey = new ConcurrentHashMap<>();

    public boolean isLockedOut(String key) {
        Attempt attempt = attemptsByKey.get(key);
        if (attempt == null) {
            return false;
        }
        if (Instant.now().isAfter(attempt.windowStart().plus(WINDOW))) {
            attemptsByKey.remove(key);
            return false;
        }
        return attempt.count() >= MAX_ATTEMPTS;
    }

    public void recordFailure(String key) {
        attemptsByKey.compute(key, (k, existing) -> {
            Instant now = Instant.now();
            if (existing == null || now.isAfter(existing.windowStart().plus(WINDOW))) {
                return new Attempt(1, now);
            }
            return new Attempt(existing.count() + 1, existing.windowStart());
        });
    }

    public void recordSuccess(String key) {
        attemptsByKey.remove(key);
    }
}
