package com.virtulab.platform.events.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtulab.platform.contracts.live.LiveProgressMessage;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class LiveProgressBroadcaster {

    public static final String REDIS_CHANNEL = "virtulab:live:progress";

    private final ReactiveStringRedisTemplate redis;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String routingKey;
    private final String n8nProgressWebhookUrl;

    public LiveProgressBroadcaster(
            ReactiveStringRedisTemplate redis,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${virtulab.rabbitmq.exchange:virtulab.tasks}") String exchange,
            @Value("${virtulab.rabbitmq.routing-key.notification:notification.email}") String routingKey,
            @Value("${virtulab.n8n.progress-webhook-url:}") String n8nProgressWebhookUrl
    ) {
        this.redis = redis;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.n8nProgressWebhookUrl = n8nProgressWebhookUrl;
    }

    public Mono<Void> broadcast(UUID eventId, String attemptId, String userId, String experimentId) {
        return Mono.fromCallable(() -> {
            LiveProgressMessage message = new LiveProgressMessage(
                    eventId.toString(),
                    attemptId,
                    userId,
                    experimentId,
                    Instant.now().toString());
            String json = objectMapper.writeValueAsString(message);
            redis.convertAndSend(REDIS_CHANNEL, json).subscribe();
            rabbitTemplate.convertAndSend(exchange, routingKey, json);
            if (n8nProgressWebhookUrl != null && !n8nProgressWebhookUrl.isBlank()) {
                WebClient.create()
                        .post()
                        .uri(n8nProgressWebhookUrl)
                        .bodyValue(message)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .onErrorComplete()
                        .subscribe();
            }
            return true;
        })
                .subscribeOn(Schedulers.boundedElastic())
                .then()
                .onErrorResume(e -> Mono.empty());
    }
}
