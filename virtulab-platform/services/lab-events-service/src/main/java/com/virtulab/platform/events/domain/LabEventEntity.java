package com.virtulab.platform.events.domain;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("lab_events")
public class LabEventEntity {

    @Id
    private UUID id;

    @Column("attempt_id")
    private String attemptId;

    @Column("user_id")
    private String userId;

    @Column("experiment_id")
    private String experimentId;

    @Column("step_id")
    private String stepId;

    @Column("time_spent_sec")
    private Integer timeSpentSec;

    @Column("progress_json")
    private Json progressJson;

    @Column("event_type")
    private String eventType;

    @Column("created_at")
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getExperimentId() { return experimentId; }
    public void setExperimentId(String experimentId) { this.experimentId = experimentId; }
    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }
    public Integer getTimeSpentSec() { return timeSpentSec; }
    public void setTimeSpentSec(Integer timeSpentSec) { this.timeSpentSec = timeSpentSec; }
    public Json getProgressJson() { return progressJson; }
    public void setProgressJson(Json progressJson) { this.progressJson = progressJson; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
