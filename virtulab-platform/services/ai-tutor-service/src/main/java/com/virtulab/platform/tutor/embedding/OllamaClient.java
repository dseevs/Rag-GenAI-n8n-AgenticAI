package com.virtulab.platform.tutor.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.virtulab.platform.tutor.config.AiProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final WebClient webClient;
    private final AiProperties props;

    public OllamaClient(WebClient ollamaWebClient, AiProperties props) {
        this.webClient = ollamaWebClient;
        this.props = props;
    }

    public Mono<float[]> embed(String text) {
        return webClient.post()
                .uri("/api/embeddings")
                .bodyValue(Map.of("model", props.ollama().embedModel(), "prompt", text))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::toVector)
                .onErrorResume(ex -> {
                    log.warn("Ollama embed failed: {}", ex.getMessage());
                    return Mono.just(hashFallback(text, props.embeddingDimensions()));
                });
    }

    public Mono<String> generate(String prompt) {
        return webClient.post()
                .uri("/api/generate")
                .bodyValue(Map.of(
                        "model", props.ollama().chatModel(),
                        "prompt", prompt,
                        "stream", false
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> node.path("response").asText(""))
                .filter(s -> !s.isBlank())
                .onErrorResume(ex -> {
                    log.warn("Ollama generate failed: {}", ex.getMessage());
                    return Mono.empty();
                });
    }

    private float[] toVector(JsonNode node) {
        JsonNode arr = node.get("embedding");
        float[] vector = new float[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            vector[i] = (float) arr.get(i).asDouble();
        }
        return vector;
    }

    static float[] hashFallback(String text, int dimensions) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] seed = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            float[] vector = new float[dimensions];
            for (int i = 0; i < dimensions; i++) {
                vector[i] = ((seed[i % seed.length] & 0xff) / 255.0f) * 2f - 1f;
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
            throw new IllegalStateException(e);
        }
    }
}
