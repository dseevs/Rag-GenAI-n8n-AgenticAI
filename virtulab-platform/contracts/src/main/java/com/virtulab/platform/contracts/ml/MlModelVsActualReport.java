package com.virtulab.platform.contracts.ml;

import java.util.List;

public record MlModelVsActualReport(
        String orgId,
        String modelVersion,
        int totalAttempts,
        int predictedComplete,
        int actuallyComplete,
        double accuracy,
        List<MlModelVsActualRow> rows
) {}
