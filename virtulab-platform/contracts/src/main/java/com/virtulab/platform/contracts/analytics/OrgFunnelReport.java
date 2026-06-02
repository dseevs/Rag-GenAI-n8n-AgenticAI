package com.virtulab.platform.contracts.analytics;

import java.util.List;

public record OrgFunnelReport(
        String orgId,
        String tenantId,
        List<OrgFunnelRow> rows,
        long generatedAtEpochMs
) {}
