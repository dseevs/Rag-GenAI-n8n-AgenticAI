package com.virtulab.platform.session.domain;

import io.r2dbc.postgresql.codec.Json;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("lab_sessions")
public class LabSessionEntity {

    @Id
    private UUID id;

    @Column("attempt_id")
    private String attemptId;

    @Column("experiment_id")
    private String experimentId;

    @Column("user_id")
    private String userId;

    @Column("tenant_id")
    private String tenantId;

    @Column("org_id")
    private String orgId;

    private String mode;
    private String lang;

    @Column("metadata_json")
    private Json metadataJson;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }
    public String getExperimentId() { return experimentId; }
    public void setExperimentId(String experimentId) { this.experimentId = experimentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public Json getMetadataJson() { return metadataJson; }
    public void setMetadataJson(Json metadataJson) { this.metadataJson = metadataJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
