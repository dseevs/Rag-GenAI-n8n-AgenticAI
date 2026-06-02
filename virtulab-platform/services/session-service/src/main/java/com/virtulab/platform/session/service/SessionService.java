package com.virtulab.platform.session.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import com.virtulab.platform.contracts.session.SessionResponse;
import com.virtulab.platform.contracts.session.StartSessionRequest;
import com.virtulab.platform.session.domain.LabSessionEntity;
import com.virtulab.platform.session.repository.LabSessionRepository;
import com.virtulab.platform.session.security.AuthContext;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class SessionService {

    private static final String CACHE_PREFIX = "virtulab:session:";

    private final LabSessionRepository repository;
    private final ReactiveStringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public SessionService(
            LabSessionRepository repository,
            ReactiveStringRedisTemplate redis,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public Mono<SessionResponse> start(StartSessionRequest request, AuthContext auth) {
        Instant now = Instant.now();
        LabSessionEntity entity = new LabSessionEntity();
        entity.setAttemptId(request.attemptId());
        entity.setExperimentId(request.experimentId());
        entity.setUserId(auth.userId());
        entity.setTenantId(auth.tenantId());
        entity.setOrgId(auth.orgId());
        entity.setMode(request.mode());
        entity.setLang(request.lang() != null ? request.lang() : "en");
        entity.setMetadataJson(Json.of("{}"));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return repository.save(entity)
                .flatMap(saved -> cache(saved).thenReturn(toResponse(saved)));
    }

    public Mono<SessionResponse> getByAttemptId(String attemptId, AuthContext auth) {
        return redis.opsForValue().get(CACHE_PREFIX + attemptId)
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, SessionResponse.class)))
                .switchIfEmpty(
                        repository.findByAttemptId(attemptId)
                                .switchIfEmpty(Mono.error(new SessionNotFoundException(attemptId)))
                                .flatMap(entity -> {
                                    if (!entity.getUserId().equals(auth.userId())) {
                                        return Mono.error(new ForbiddenSessionAccessException());
                                    }
                                    return cache(entity).thenReturn(toResponse(entity));
                                })
                );
    }

    private Mono<Void> cache(LabSessionEntity entity) {
        try {
            String json = objectMapper.writeValueAsString(toResponse(entity));
            return redis.opsForValue()
                    .set(CACHE_PREFIX + entity.getAttemptId(), json, Duration.ofHours(24))
                    .then();
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    private SessionResponse toResponse(LabSessionEntity e) {
        return new SessionResponse(
                e.getId(),
                e.getAttemptId(),
                e.getExperimentId(),
                e.getUserId(),
                e.getTenantId(),
                e.getOrgId(),
                e.getMode(),
                e.getLang(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
