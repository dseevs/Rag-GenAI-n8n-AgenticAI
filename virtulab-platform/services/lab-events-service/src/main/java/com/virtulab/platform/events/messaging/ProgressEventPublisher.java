package com.virtulab.platform.events.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class ProgressEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public ProgressEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${virtulab.kafka.topic.progress:lab.progress.v1}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public Mono<Void> publish(UUID eventId, String attemptId, String userId, String experimentId, Map<String, Object> progress) {
        return Mono.fromRunnable(() -> {
            try {
                String payload = objectMapper.writeValueAsString(Map.of(
                        "eventId", eventId.toString(),
                        "attemptId", attemptId,
                        "userId", userId,
                        "experimentId", experimentId,
                        "progress", progress,
                        "timestamp", Instant.now().toString()
                ));
                kafkaTemplate.send(topic, attemptId, payload);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
