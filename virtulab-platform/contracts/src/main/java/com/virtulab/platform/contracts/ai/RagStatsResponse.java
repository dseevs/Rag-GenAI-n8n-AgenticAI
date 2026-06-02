package com.virtulab.platform.contracts.ai;

import java.util.Map;

public record RagStatsResponse(
        int activeCorpusVersion,
        long totalChunks,
        Map<String, Long> chunksByExperiment
) {}
