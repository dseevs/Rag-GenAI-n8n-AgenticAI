package com.virtulab.platform.contracts.analytics;

public record ExperimentSummaryReport(
        String experimentId,
        long distinctUsers,
        long totalEvents,
        long totalTimeSpentSec,
        double avgProgressPct
) {}
