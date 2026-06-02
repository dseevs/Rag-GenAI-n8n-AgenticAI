package com.virtulab.platform.analytics.ingest.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtulab.platform.analytics.ingest.store.AnalyticsFactRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsKafkaConsumers {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsKafkaConsumers.class);

    private final ObjectMapper objectMapper;
    private final AnalyticsFactRepository repository;

    public AnalyticsKafkaConsumers(ObjectMapper objectMapper, AnalyticsFactRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @KafkaListener(topics = "${virtulab.kafka.topics.progress}", groupId = "analytics-ingest")
    public void onProgress(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            UUID eventId = UUID.fromString(node.path("eventId").asText());
            String attemptId = node.path("attemptId").asText();
            String userId = node.path("userId").asText();
            String experimentId = node.path("experimentId").asText();
            Instant eventTs = Instant.parse(node.path("timestamp").asText());

            String tenantId = "tenant-dev";
            String orgId = "org-dev";
            var session = repository.findSessionContext(attemptId);
            if (session.isPresent()) {
                tenantId = session.get().tenantId();
                orgId = session.get().orgId();
            }
            repository.upsertUser(userId, tenantId, orgId);

            JsonNode progress = node.path("progress");
            String tab = progress.path("tab").asText(null);
            Integer pct = progress.has("percentage") ? progress.path("percentage").asInt() : null;

            repository.insertProgress(
                    eventId,
                    attemptId,
                    userId,
                    experimentId,
                    tenantId,
                    orgId,
                    null,
                    null,
                    tab,
                    pct,
                    eventTs);
        } catch (Exception e) {
            log.warn("Failed to ingest progress event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "${virtulab.kafka.topics.ai}", groupId = "analytics-ingest")
    public void onAi(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String userId = node.path("userId").asText();
            String experimentId = node.path("experimentId").asText(null);
            String attemptId = node.path("attemptId").asText(null);
            String eventType = node.path("type").asText("unknown");
            String source = node.path("source").asText(null);
            Long latencyMs = node.has("latencyMs") ? node.path("latencyMs").asLong() : null;
            Instant eventTs = node.has("timestamp")
                    ? Instant.parse(node.path("timestamp").asText())
                    : Instant.now();

            repository.insertAi(userId, experimentId, attemptId, eventType, source, latencyMs, eventTs);
            repository.upsertUser(userId, "tenant-dev", "org-dev");
        } catch (Exception e) {
            log.warn("Failed to ingest AI event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "${virtulab.kafka.topics.quiz}", groupId = "analytics-ingest")
    public void onQuiz(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String attemptId = node.path("attemptId").asText();
            String userId = node.path("userId").asText();
            String experimentId = node.path("experimentId").asText("v1-chemistry");
            String mode = node.path("mode").asText("PRACTICE");
            int score = node.path("score").asInt();
            int total = node.path("totalQuestions").asInt();
            int correct = node.path("correctCount").asInt();
            Instant eventTs = node.has("timestamp")
                    ? Instant.parse(node.path("timestamp").asText())
                    : Instant.now();

            repository.insertQuiz(attemptId, userId, experimentId, mode, score, total, correct, eventTs);
            repository.upsertUser(userId, "tenant-dev", "org-dev");
        } catch (Exception e) {
            log.warn("Failed to ingest quiz event: {}", e.getMessage());
        }
    }
}
