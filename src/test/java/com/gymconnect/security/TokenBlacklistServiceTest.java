package com.gymconnect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    void isBlacklisted_returnsFalse_forUnknownToken() {
        assertFalse(tokenBlacklistService.isBlacklisted("unknown-token"));
    }

    @Test
    void isBlacklisted_returnsTrue_afterBlacklist() {
        tokenBlacklistService.blacklist("some-token");

        assertTrue(tokenBlacklistService.isBlacklisted("some-token"));
    }

    @Test
    void isBlacklisted_returnsFalse_forDifferentToken() {
        tokenBlacklistService.blacklist("token-a");

        assertFalse(tokenBlacklistService.isBlacklisted("token-b"));
    }
}
