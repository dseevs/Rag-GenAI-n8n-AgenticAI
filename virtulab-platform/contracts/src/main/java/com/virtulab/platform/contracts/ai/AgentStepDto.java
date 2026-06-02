package com.virtulab.platform.contracts.ai;

public record AgentStepDto(
        int stepIndex,
        String toolName,
        String summary
) {}
