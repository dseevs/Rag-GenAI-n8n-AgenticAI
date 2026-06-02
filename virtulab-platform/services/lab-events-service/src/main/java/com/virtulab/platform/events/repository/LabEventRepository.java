package com.virtulab.platform.events.repository;

import com.virtulab.platform.events.domain.LabEventEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LabEventRepository extends ReactiveCrudRepository<LabEventEntity, java.util.UUID> {}
