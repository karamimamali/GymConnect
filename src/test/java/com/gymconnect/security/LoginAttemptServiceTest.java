package com.gymconnect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    void isBlocked_returnsFalse_forNewUser() {
        assertFalse(loginAttemptService.isBlocked("user1"));
    }

    @Test
    void isBlocked_returnsFalse_afterOneFailedAttempt() {
        loginAttemptService.loginFailed("user1");

        assertFalse(loginAttemptService.isBlocked("user1"));
    }

    @Test
    void isBlocked_returnsFalse_afterTwoFailedAttempts() {
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");

        assertFalse(loginAttemptService.isBlocked("user1"));
    }

    @Test
    void isBlocked_returnsTrue_afterThreeFailedAttempts() {
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");

        assertTrue(loginAttemptService.isBlocked("user1"));
    }

    @Test
    void isBlocked_returnsFalse_afterSuccessfulLogin() {
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");

        loginAttemptService.loginSucceeded("user1");

        assertFalse(loginAttemptService.isBlocked("user1"));
    }

    @Test
    void isBlocked_tracksUsersSeparately() {
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");

        assertFalse(loginAttemptService.isBlocked("user2"));
    }

    @Test
    void loginSucceeded_resetsPreviousFailures() {
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");

        loginAttemptService.loginSucceeded("user1");
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");

        assertFalse(loginAttemptService.isBlocked("user1"));
    }
}
