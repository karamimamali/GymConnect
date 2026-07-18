package com.gymconnect.workload.cucumber;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Mints JWT tokens the way the main service does (HS256 over the shared
 * secret), so scenarios can exercise the service-to-service authentication
 * contract without booting the main service.
 */
@Component
public class ServiceTokenFactory {

    /** Any Base64 256-bit key that differs from the shared one. */
    private static final String FOREIGN_SECRET =
            "YW4gZW50aXJlbHkgZGlmZmVyZW50IHNlY3JldCBrZXkgZm9yIHRlc3RzITE=";

    private static final long VALIDITY_MS = 60_000;

    private final String sharedSecret;

    public ServiceTokenFactory(@Value("${jwt.secret}") String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    /** @return a token the workload service must accept. */
    public String issuedWithSharedSecret(String subject) {
        return token(subject, sharedSecret);
    }

    /** @return a well-formed token signed with the wrong key — must be rejected. */
    public String issuedWithForeignKey(String subject) {
        return token(subject, FOREIGN_SECRET);
    }

    private String token(String subject, String base64Secret) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + VALIDITY_MS))
                .signWith(key)
                .compact();
    }
}
