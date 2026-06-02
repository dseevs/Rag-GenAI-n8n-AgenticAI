package com.virtulab.platform.contracts.analytics;

import java.util.List;

public record AiVsProgressReport(
        String orgId,
        List<AiUsageRow> rows
) {}
