package com.freshmart.security;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 5;

    private static class Attempt {
        int count;
        LocalDateTime lockUntil;
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        Attempt a = attempts.get(username);
        if (a == null) return false;

        if (a.lockUntil != null && a.lockUntil.isAfter(LocalDateTime.now())) {
            return true;
        }

        if (a.lockUntil != null && a.lockUntil.isBefore(LocalDateTime.now())) {
            attempts.remove(username);
        }

        return false;
    }

    public void loginFailed(String username) {
        Attempt a = attempts.computeIfAbsent(username, k -> new Attempt());
        a.count++;

        if (a.count >= MAX_ATTEMPTS) {
            a.lockUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
        }
    }

    public void loginSuccess(String username) {
        attempts.remove(username);
    }
}