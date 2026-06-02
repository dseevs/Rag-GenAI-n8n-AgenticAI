package com.virtulab.platform.tutor.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AiEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AiEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public AiEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${virtulab.kafka.topic.ai}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(String type, String userId, String experimentId, String attemptId, String source, long latencyMs) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "userId", userId,
                    "experimentId", experimentId != null ? experimentId : "",
                    "attemptId", attemptId != null ? attemptId : "",
                    "source", source,
                    "latencyMs", latencyMs,
                    "timestamp", Instant.now().toString()
            ));
            kafkaTemplate.send(topic, attemptId != null ? attemptId : userId, payload);
        } catch (Exception e) {
            log.warn("Failed to publish lab.ai.v1: {}", e.getMessage());
        }
    }
}
