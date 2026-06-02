package com.virtulab.platform.contracts.ai;

import jakarta.validation.constraints.NotBlank;

public record FoodForThoughtRequest(
        @NotBlank String topic,
        String experimentId,
        String lang
) {
    public FoodForThoughtRequest {
        if (lang == null || lang.isBlank()) {
            lang = "en";
        }
    }
}
