package com.virtulab.platform.contracts.live;

public record LiveProgressMessage(
        String eventId,
        String attemptId,
        String userId,
        String experimentId,
        String timestamp
) {}
