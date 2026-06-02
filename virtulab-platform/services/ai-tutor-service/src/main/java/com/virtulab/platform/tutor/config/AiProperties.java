package com.virtulab.platform.tutor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "virtulab.ai")
public record AiProperties(int embeddingDimensions, Ollama ollama) {
    public record Ollama(String baseUrl, String embedModel, String chatModel) {}
}
