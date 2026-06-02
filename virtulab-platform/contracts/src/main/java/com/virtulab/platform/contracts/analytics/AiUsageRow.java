package com.virtulab.platform.contracts.analytics;

public record AiUsageRow(
        String userId,
        String experimentId,
        long aiEventCount,
        long progressEventCount,
        double avgAiLatencyMs
) {}
