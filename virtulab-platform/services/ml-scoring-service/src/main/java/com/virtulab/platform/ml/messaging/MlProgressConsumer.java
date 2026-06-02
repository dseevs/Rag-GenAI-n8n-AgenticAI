package com.virtulab.platform.ml.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtulab.platform.ml.service.MlScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MlProgressConsumer {

    private static final Logger log = LoggerFactory.getLogger(MlProgressConsumer.class);

    private final ObjectMapper objectMapper;
    private final MlScoringService scoringService;

    public MlProgressConsumer(ObjectMapper objectMapper, MlScoringService scoringService) {
        this.objectMapper = objectMapper;
        this.scoringService = scoringService;
    }

    @KafkaListener(topics = "${virtulab.kafka.topics.progress}", groupId = "ml-scoring")
    public void onProgress(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String attemptId = node.path("attemptId").asText(null);
            if (attemptId == null || attemptId.isBlank()) {
                return;
            }
            scoringService.scoreAttempt(attemptId);
        } catch (Exception e) {
            log.debug("ML auto-score skipped: {}", e.getMessage());
        }
    }
}
