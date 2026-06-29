package com.gymconnect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET =
            "dGhpcyBpcyBhIHNlY3VyZSBzZWNyZXQga2V5IGZvciBKV1QgdG9rZW5zITEyMzQ=";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtTokenProvider.generateToken("user1");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtTokenProvider.generateToken("Alice.Brown");

        String username = jwtTokenProvider.extractUsername(token);

        assertEquals("Alice.Brown", username);
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        String token = jwtTokenProvider.generateToken("user1");

        assertTrue(jwtTokenProvider.isTokenValid(token));
    }

    @Test
    void isTokenValid_returnsFalseForTamperedToken() {
        String token = jwtTokenProvider.generateToken("user1") + "tampered";

        assertFalse(jwtTokenProvider.isTokenValid(token));
    }

    @Test
    void isTokenValid_returnsFalseForEmptyToken() {
        assertFalse(jwtTokenProvider.isTokenValid(""));
    }

    @Test
    void generateToken_differentUsersProduceDifferentTokens() {
        String token1 = jwtTokenProvider.generateToken("user1");
        String token2 = jwtTokenProvider.generateToken("user2");

        assertFalse(token1.equals(token2));
    }
}
