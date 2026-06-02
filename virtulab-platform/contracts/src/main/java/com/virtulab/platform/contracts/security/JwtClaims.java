package com.virtulab.platform.contracts.security;

import java.time.Instant;
import java.util.List;

public record JwtClaims(
        String userId,
        List<String> roles,
        String tenantId,
        String orgId,
        Instant expiresAt
) {}
