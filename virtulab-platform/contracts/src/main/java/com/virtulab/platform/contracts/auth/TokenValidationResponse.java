package com.virtulab.platform.contracts.auth;

import java.time.Instant;
import java.util.List;

public record TokenValidationResponse(
        boolean valid,
        String userId,
        List<String> roles,
        String tenantId,
        String orgId,
        Instant expiresAt,
        String message
) {
    public static TokenValidationResponse invalid(String message) {
        return new TokenValidationResponse(false, null, List.of(), null, null, null, message);
    }
}
