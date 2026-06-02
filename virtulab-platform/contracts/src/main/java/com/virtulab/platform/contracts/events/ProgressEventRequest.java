package com.virtulab.platform.contracts.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ProgressEventRequest(
        @NotBlank String experimentId,
        @NotBlank String attemptId,
        @NotBlank String stepId,
        Integer timeSpentSec,
        @NotNull Map<String, Object> progress
) {}
