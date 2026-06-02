package com.virtulab.platform.contracts.ml;

public record MlModelVsActualRow(
        String attemptId,
        String userId,
        double predictedScore,
        int actualProgressPct,
        boolean predictedComplete,
        boolean actuallyComplete
) {}
