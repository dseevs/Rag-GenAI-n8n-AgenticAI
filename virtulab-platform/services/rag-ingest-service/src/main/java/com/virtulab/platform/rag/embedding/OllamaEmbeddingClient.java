package com.virtulab.platform.rag.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.virtulab.platform.rag.config.RagProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OllamaEmbeddingClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaEmbeddingClient.class);

    private final WebClient ollamaWebClient;
    private final RagProperties props;

    public OllamaEmbeddingClient(WebClient ollamaWebClient, RagProperties props) {
        this.ollamaWebClient = ollamaWebClient;
        this.props = props;
    }

    public Mono<float[]> embed(String text) {
        return ollamaWebClient.post()
                .uri("/api/embeddings")
                .bodyValue(Map.of("model", props.ollama().embedModel(), "prompt", text))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> {
                    JsonNode arr = node.get("embedding");
                    if (arr == null || !arr.isArray()) {
                        throw new IllegalStateException("Ollama embedding response missing array");
                    }
                    float[] vector = new float[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        vector[i] = (float) arr.get(i).asDouble();
                    }
                    return vector;
                })
                .onErrorResume(ex -> {
                    log.warn("Ollama embed failed, using hash fallback: {}", ex.getMessage());
                    return Mono.just(hashFallback(text, props.embeddingDimensions()));
                });
    }

    static float[] hashFallback(String text, int dimensions) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] seed = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            float[] vector = new float[dimensions];
            for (int i = 0; i < dimensions; i++) {
                int b = seed[i % seed.length] & 0xff;
                vector[i] = (b / 255.0f) * 2f - 1f;
            }
            float norm = 0f;
            for (float v : vector) {
                norm += v * v;
            }
            norm = (float) Math.sqrt(norm);
            if (norm > 0) {
                for (int i = 0; i < vector.length; i++) {
                    vector[i] /= norm;
                }
            }
            return vector;
        } catch (Exception e) {
            throw new IllegalStateException("hash fallback failed", e);
        }
    }
}
