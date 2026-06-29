package com.gymconnect.workload.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET =
            "dGhpcyBpcyBhIHNlY3VyZSBzZWNyZXQga2V5IGZvciBKV1QgdG9rZW5zITEyMzQ=";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    private SecretKey keyFor(String base64Secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    private String tokenWith(String subject, SecretKey key, Date issued, Date expiry) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(issued)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    @Test
    void extractSubjectIfValid_shouldReturnSubject_forValidToken() {
        Date now = new Date();
        String token = tokenWith("gym-main-service", keyFor(SECRET), now,
                new Date(now.getTime() + 60_000));

        Optional<String> subject = jwtService.extractSubjectIfValid(token);

        assertTrue(subject.isPresent());
        assertEquals("gym-main-service", subject.get());
    }

    @Test
    void extractSubjectIfValid_shouldBeEmpty_forGarbageToken() {
        assertTrue(jwtService.extractSubjectIfValid("not-a-jwt").isEmpty());
    }

    @Test
    void extractSubjectIfValid_shouldBeEmpty_whenSignedWithDifferentKey() {
        String otherSecret = "YW5vdGhlci1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW5zLTEyMzQ1Njc4OTA=";
        Date now = new Date();
        String token = tokenWith("intruder", keyFor(otherSecret), now,
                new Date(now.getTime() + 60_000));

        assertTrue(jwtService.extractSubjectIfValid(token).isEmpty());
    }

    @Test
    void extractSubjectIfValid_shouldBeEmpty_forExpiredToken() {
        Date past = new Date(System.currentTimeMillis() - 120_000);
        String token = tokenWith("gym-main-service", keyFor(SECRET), past,
                new Date(System.currentTimeMillis() - 60_000));

        assertTrue(jwtService.extractSubjectIfValid(token).isEmpty());
    }
}
