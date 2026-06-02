package com.virtulab.platform.contracts.ai;

import java.util.List;

public record AgentRunResponse(
        String runId,
        String agentType,
        String status,
        String finalAnswer,
        List<AgentStepDto> steps,
        List<RagCitation> citations
) {}
