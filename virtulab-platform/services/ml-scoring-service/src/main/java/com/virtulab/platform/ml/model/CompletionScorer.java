package com.virtulab.platform.ml.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CompletionScorer {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final String defaultModelVersion;

    public CompletionScorer(
            JdbcTemplate jdbc,
            ObjectMapper objectMapper,
            @Value("${virtulab.ml.model-version}") String defaultModelVersion
    ) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.defaultModelVersion = defaultModelVersion;
    }

    public ScoredAttempt score(AttemptFeatureExtractor.AttemptFeatures features) {
        JsonNode weights = loadWeights(defaultModelVersion);
        double z = weights.path("bias").asDouble(-0.35);
        z += weights.path("maxProgressPct").asDouble(0.045) * features.features().path("maxProgressPct").asInt();
        z += weights.path("aiEventCount").asDouble(0.18) * features.features().path("aiEventCount").asInt();
        z += weights.path("progressEventCount").asDouble(0.05)
                * features.features().path("progressEventCount").asInt();
        double score = sigmoid(z);
        return new ScoredAttempt(features, defaultModelVersion, score);
    }

    private JsonNode loadWeights(String modelVersion) {
        String json = jdbc.queryForObject(
                "SELECT weights_json::text FROM ml.model_registry WHERE model_version = ? AND active = TRUE",
                String.class,
                modelVersion);
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid model weights for " + modelVersion, e);
        }
    }

    private static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    public record ScoredAttempt(
            AttemptFeatureExtractor.AttemptFeatures features,
            String modelVersion,
            double score
    ) {}
}
