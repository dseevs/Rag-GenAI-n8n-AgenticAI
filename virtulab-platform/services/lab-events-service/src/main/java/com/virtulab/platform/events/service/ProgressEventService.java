package com.virtulab.platform.events.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import com.virtulab.platform.contracts.events.EventAcceptedResponse;
import com.virtulab.platform.contracts.events.ProgressEventRequest;
import com.virtulab.platform.events.domain.LabEventEntity;
import com.virtulab.platform.events.messaging.LiveProgressBroadcaster;
import com.virtulab.platform.events.messaging.ProgressEventPublisher;
import com.virtulab.platform.events.repository.LabEventRepository;
import com.virtulab.platform.events.security.AuthContext;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class ProgressEventService {

    private static final String IDEM_PREFIX = "virtulab:idempotency:";

    private final LabEventRepository repository;
    private final ReactiveStringRedisTemplate redis;
    private final ProgressEventPublisher publisher;
    private final LiveProgressBroadcaster liveBroadcaster;
    private final ObjectMapper objectMapper;

    public ProgressEventService(
            LabEventRepository repository,
            ReactiveStringRedisTemplate redis,
            ProgressEventPublisher publisher,
            LiveProgressBroadcaster liveBroadcaster,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.redis = redis;
        this.publisher = publisher;
        this.liveBroadcaster = liveBroadcaster;
        this.objectMapper = objectMapper;
    }

    public Mono<EventAcceptedResponse> ingest(
            ProgressEventRequest request,
            AuthContext auth,
            String idempotencyKey
    ) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            return redis.opsForValue().get(IDEM_PREFIX + idempotencyKey)
                    .flatMap(existing -> Mono.just(new EventAcceptedResponse(UUID.fromString(existing), true)))
                    .switchIfEmpty(persistAndCache(request, auth, idempotencyKey));
        }
        return persistAndCache(request, auth, null);
    }

    private Mono<EventAcceptedResponse> persistAndCache(
            ProgressEventRequest request,
            AuthContext auth,
            String idempotencyKey
    ) {
        LabEventEntity entity = new LabEventEntity();
        entity.setAttemptId(request.attemptId());
        entity.setUserId(auth.userId());
        entity.setExperimentId(request.experimentId());
        entity.setStepId(request.stepId());
        entity.setTimeSpentSec(request.timeSpentSec());
        entity.setEventType("PROGRESS");
        entity.setCreatedAt(Instant.now());
        try {
            entity.setProgressJson(Json.of(objectMapper.writeValueAsString(request.progress())));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        return repository.save(entity)
                .flatMap(saved -> publisher.publish(
                                saved.getId(),
                                saved.getAttemptId(),
                                saved.getUserId(),
                                saved.getExperimentId(),
                                request.progress()
                        ).then(liveBroadcaster.broadcast(
                                saved.getId(),
                                saved.getAttemptId(),
                                saved.getUserId(),
                                saved.getExperimentId()))
                        .thenReturn(saved))
                .flatMap(saved -> {
                    EventAcceptedResponse response = new EventAcceptedResponse(saved.getId(), false);
                    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                        return redis.opsForValue()
                                .set(IDEM_PREFIX + idempotencyKey, saved.getId().toString(), Duration.ofDays(1))
                                .thenReturn(response);
                    }
                    return Mono.just(response);
                });
    }
}
