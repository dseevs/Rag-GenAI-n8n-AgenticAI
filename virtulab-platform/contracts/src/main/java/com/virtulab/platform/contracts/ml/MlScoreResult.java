package com.virtulab.platform.contracts.ml;

public record MlScoreResult(
        String attemptId,
        String modelVersion,
        double score,
        String featuresJson,
        boolean stored
) {}
