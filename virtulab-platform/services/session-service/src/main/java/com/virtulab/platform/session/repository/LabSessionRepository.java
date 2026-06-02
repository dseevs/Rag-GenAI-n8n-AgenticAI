package com.virtulab.platform.session.repository;

import com.virtulab.platform.session.domain.LabSessionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LabSessionRepository extends ReactiveCrudRepository<LabSessionEntity, java.util.UUID> {
    Mono<LabSessionEntity> findByAttemptId(String attemptId);
}
