package com.virtulab.platform.contracts.session;

import jakarta.validation.constraints.NotBlank;

public record StartSessionRequest(
        @NotBlank String experimentId,
        @NotBlank String attemptId,
        @NotBlank String mode,
        String lang
) {}
