package com.virtulab.platform.contracts.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Shared HS256 JWT helper for Phase 1 dev mode (same secret across services).
 */
public final class DevJwtService {

    private final SecretKey key;

    public DevJwtService(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String issue(String userId, List<String> roles, String tenantId, String orgId, long expiresInSeconds) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresInSeconds);
        return Jwts.builder()
                .subject(userId)
                .claim("roles", roles)
                .claim("tenantId", tenantId)
                .claim("orgId", orgId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Optional<JwtClaims> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            return Optional.of(new JwtClaims(
                    claims.getSubject(),
                    roles != null ? roles : List.of(),
                    claims.get("tenantId", String.class),
                    claims.get("orgId", String.class),
                    claims.getExpiration().toInstant()
            ));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
