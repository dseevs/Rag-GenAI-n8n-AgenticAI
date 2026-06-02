package com.virtulab.platform.quiz.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.virtulab.platform.contracts.quiz.QuizSubmitResponse;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class QuizEventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;
    private final String topic;

    public QuizEventPublisher(
            KafkaTemplate<String, String> kafka,
            ObjectMapper objectMapper,
            @Value("${virtulab.kafka.topics.quiz}") String topic
    ) {
        this.kafka = kafka;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(String userId, String experimentId, QuizSubmitResponse response) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("attemptId", response.attemptId());
        node.put("userId", userId);
        node.put("experimentId", experimentId);
        node.put("score", response.score());
        node.put("totalQuestions", response.totalQuestions());
        node.put("correctCount", response.correctCount());
        node.put("mode", response.mode());
        node.put("timestamp", Instant.now().toString());
        kafka.send(topic, response.attemptId(), node.toString());
    }
}
