package com.virtulab.platform.contracts.ai;

import jakarta.validation.constraints.NotBlank;

public record AgentRunRequest(
        @NotBlank String agentType,
        String attemptId,
        @NotBlank String experimentId,
        String lang,
        String userMessage
) {
    public AgentRunRequest {
        if (lang == null || lang.isBlank()) {
            lang = "en";
        }
    }
}
