package com.virtulab.platform.contracts.ml;

import java.util.List;

public record MlBatchScoreResult(
        int scored,
        String modelVersion,
        List<MlScoreResult> results
) {}
