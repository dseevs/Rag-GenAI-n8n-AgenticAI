package com.virtulab.platform.contracts.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

/** {@code detail} avoids Jackson/WebFlux clash with Throwable#getMessage() on accessor {@code message()}. */
public record RagJobStatusResponse(
        String jobId,
        String status,
        int chunksIndexed,
        @JsonProperty("message") String detail
) {}
