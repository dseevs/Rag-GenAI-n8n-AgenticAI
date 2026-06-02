package com.virtulab.platform.contracts.ml;

import java.time.Instant;

public record MlPredictionDto(
        String id,
        String attemptId,
        String userId,
        String experimentId,
        String modelVersion,
        double score,
        String featuresJson,
        Instant predictedAt
) {}
