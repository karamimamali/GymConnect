package com.gymconnect.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MINUTES = 5;

    private final ConcurrentHashMap<String, AttemptRecord> attemptCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attemptCache.remove(username);
        logger.debug("Cleared failed login attempts for user: {}", username);
    }

    public void loginFailed(String username) {
        AttemptRecord record = attemptCache.computeIfAbsent(username, k -> new AttemptRecord());
        int attempts = record.incrementAndGet();
        if (attempts >= MAX_ATTEMPTS) {
            record.setBlockedUntil(LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES));
            logger.warn("User '{}' blocked for {} minutes after {} failed login attempts",
                    username, BLOCK_DURATION_MINUTES, attempts);
        } else {
            logger.warn("Failed login attempt {}/{} for user: {}", attempts, MAX_ATTEMPTS, username);
        }
    }

    public boolean isBlocked(String username) {
        AttemptRecord record = attemptCache.get(username);
        if (record == null || record.getBlockedUntil() == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(record.getBlockedUntil())) {
            attemptCache.remove(username);
            logger.info("Block expired for user: {}", username);
            return false;
        }
        return true;
    }

    private static class AttemptRecord {
        private int attempts;
        private LocalDateTime blockedUntil;

        int incrementAndGet() {
            return ++attempts;
        }

        int getAttempts() {
            return attempts;
        }

        LocalDateTime getBlockedUntil() {
            return blockedUntil;
        }

        void setBlockedUntil(LocalDateTime blockedUntil) {
            this.blockedUntil = blockedUntil;
        }
    }
}
