package com.virtulab.platform.contracts.audit;

import java.time.Instant;

public record AuditEventDto(
        String id,
        String eventType,
        String sourceTopic,
        String userId,
        String attemptId,
        String experimentId,
        String payloadJson,
        Instant recordedAt
) {}
