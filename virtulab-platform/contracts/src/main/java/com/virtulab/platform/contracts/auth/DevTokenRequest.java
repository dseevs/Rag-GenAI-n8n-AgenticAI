package com.virtulab.platform.contracts.auth;

import java.util.List;

public record DevTokenRequest(
        String userId,
        List<String> roles,
        String tenantId,
        String orgId,
        Long expiresInSeconds
) {}
