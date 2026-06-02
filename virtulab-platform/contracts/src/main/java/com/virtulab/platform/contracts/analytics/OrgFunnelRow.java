package com.virtulab.platform.contracts.analytics;

import java.util.List;

public record OrgFunnelRow(
        String orgName,
        String userId,
        String experimentId,
        long sessionCount,
        long eventCount,
        long totalTimeSpentSec
) {}
