package com.virtulab.platform.contracts.audit;

import java.util.List;

public record AuditEventPage(
        long total,
        List<AuditEventDto> events
) {}
