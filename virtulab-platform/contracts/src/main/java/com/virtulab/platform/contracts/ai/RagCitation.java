package com.virtulab.platform.contracts.ai;

public record RagCitation(
        String chunkId,
        String excerpt,
        String docType
) {}
