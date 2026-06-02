package com.virtulab.platform.contracts.ai;

import jakarta.validation.constraints.NotBlank;

public record RagAskRequest(
        String attemptId,
        @NotBlank String experimentId,
        @NotBlank String question,
        String lang
) {
    public RagAskRequest {
        if (lang == null || lang.isBlank()) {
            lang = "en";
        }
    }
}
