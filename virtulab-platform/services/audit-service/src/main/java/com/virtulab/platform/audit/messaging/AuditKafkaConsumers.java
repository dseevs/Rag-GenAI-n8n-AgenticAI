package com.virtulab.platform.audit.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtulab.platform.audit.store.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditKafkaConsumers {

    private static final Logger log = LoggerFactory.getLogger(AuditKafkaConsumers.class);

    private final ObjectMapper objectMapper;
    private final AuditRepository repository;

    public AuditKafkaConsumers(ObjectMapper objectMapper, AuditRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @KafkaListener(topics = "${virtulab.kafka.topics.progress}", groupId = "audit-service")
    public void onProgress(String payload) {
        ingest("PROGRESS", "lab.progress.v1", payload);
    }

    @KafkaListener(topics = "${virtulab.kafka.topics.ai}", groupId = "audit-service")
    public void onAi(String payload) {
        ingest("AI", "lab.ai.v1", payload);
    }

    private void ingest(String eventType, String topic, String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            repository.insert(
                    eventType,
                    topic,
                    text(node, "userId"),
                    text(node, "attemptId"),
                    text(node, "experimentId"),
                    payload);
        } catch (Exception e) {
            log.warn("Audit ingest failed for {}: {}", topic, e.getMessage());
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && !v.isNull() ? v.asText() : null;
    }
}
