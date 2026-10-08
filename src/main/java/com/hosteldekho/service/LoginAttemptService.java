package com.hosteldekho.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_SECONDS = 15 * 60;
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) return false;
        synchronized (attempt) {
            if (attempt.expiresAt.isBefore(Instant.now())) {
                attempts.remove(key, attempt);
                return false;
            }
            return attempt.failures >= MAX_FAILURES;
        }
    }

    public void failed(String key) {
        Instant now = Instant.now();
        Attempt attempt = attempts.compute(key, (ignored, current) ->
            current == null || current.expiresAt.isBefore(now)
                ? new Attempt(0, now.plusSeconds(WINDOW_SECONDS)) : current);
        synchronized (attempt) {
            if (attempt.failures < MAX_FAILURES) attempt.failures++;
        }
    }

    public void succeeded(String key) {
        attempts.remove(key);
    }

    private static final class Attempt {
        private int failures;
        private final Instant expiresAt;

        private Attempt(int failures, Instant expiresAt) {
            this.failures = failures;
            this.expiresAt = expiresAt;
        }
    }
}
