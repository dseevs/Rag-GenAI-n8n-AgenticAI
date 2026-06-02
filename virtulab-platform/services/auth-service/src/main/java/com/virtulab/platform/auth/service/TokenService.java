package com.virtulab.platform.auth.service;

import com.virtulab.platform.contracts.auth.DevTokenRequest;
import com.virtulab.platform.contracts.auth.DevTokenResponse;
import com.virtulab.platform.contracts.auth.TokenValidationResponse;
import com.virtulab.platform.contracts.security.DevJwtService;
import com.virtulab.platform.contracts.security.JwtClaims;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class TokenService {

    private static final String DENY_PREFIX = "virtulab:deny:";

    private final DevJwtService jwtService;
    private final ReactiveStringRedisTemplate redis;
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker redisCircuitBreaker;
    private final long defaultTtlSeconds;

    public TokenService(
            DevJwtService jwtService,
            ReactiveStringRedisTemplate redis,
            io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry,
            @Value("${virtulab.jwt.default-ttl-seconds:3600}") long defaultTtlSeconds
    ) {
        this.jwtService = jwtService;
        this.redis = redis;
        this.redisCircuitBreaker = circuitBreakerRegistry.circuitBreaker("redis");
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    public DevTokenResponse issueDevToken(DevTokenRequest request) {
        long ttl = request.expiresInSeconds() != null ? request.expiresInSeconds() : defaultTtlSeconds;
        List<String> roles = request.roles() != null && !request.roles().isEmpty()
                ? request.roles()
                : List.of("ROLE_CLIENT");
        String token = jwtService.issue(
                request.userId(),
                roles,
                request.tenantId() != null ? request.tenantId() : "tenant-dev",
                request.orgId() != null ? request.orgId() : "org-dev",
                ttl
        );
        return new DevTokenResponse(token, "Bearer", ttl);
    }

    public Mono<TokenValidationResponse> validate(String token) {
        if (token == null || token.isBlank()) {
            return Mono.just(TokenValidationResponse.invalid("Missing token"));
        }
        String bare = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();

        return isDenied(bare)
                .flatMap(denied -> {
                    if (Boolean.TRUE.equals(denied)) {
                        return Mono.just(TokenValidationResponse.invalid("Token revoked"));
                    }
                    return Mono.fromCallable(() -> jwtService.parse(bare))
                            .map(opt -> opt.map(this::toResponse)
                                    .orElseGet(() -> TokenValidationResponse.invalid("Invalid or expired token")));
                });
    }

    private TokenValidationResponse toResponse(JwtClaims claims) {
        return new TokenValidationResponse(
                true,
                claims.userId(),
                claims.roles(),
                claims.tenantId(),
                claims.orgId(),
                claims.expiresAt(),
                null
        );
    }

    private Mono<Boolean> isDenied(String token) {
        return redis.hasKey(DENY_PREFIX + token)
                .transformDeferred(CircuitBreakerOperator.of(redisCircuitBreaker))
                .onErrorReturn(false);
    }

    public Mono<Void> revoke(String token, Duration ttl) {
        String bare = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        return redis.opsForValue().set(DENY_PREFIX + bare, "1", ttl)
                .transformDeferred(CircuitBreakerOperator.of(redisCircuitBreaker))
                .then();
    }
}
