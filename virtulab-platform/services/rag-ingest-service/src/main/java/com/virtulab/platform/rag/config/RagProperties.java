package com.virtulab.platform.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "virtulab.rag")
public record RagProperties(
        String corpusPath,
        int embeddingDimensions,
        Ollama ollama
) {
    public record Ollama(String baseUrl, String embedModel) {}
}
