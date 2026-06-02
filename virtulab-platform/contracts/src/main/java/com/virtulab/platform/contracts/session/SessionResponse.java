package com.virtulab.platform.contracts.session;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String attemptId,
        String experimentId,
        String userId,
        String tenantId,
        String orgId,
        String mode,
        String lang,
        Instant createdAt,
        Instant updatedAt
) {}
