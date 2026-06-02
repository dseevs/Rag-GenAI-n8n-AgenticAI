CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE IF NOT EXISTS audit.audit_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type      VARCHAR(64) NOT NULL,
    source_topic    VARCHAR(128) NOT NULL,
    user_id         VARCHAR(128),
    attempt_id      VARCHAR(128),
    experiment_id   VARCHAR(128),
    payload_json    JSONB NOT NULL,
    recorded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_events_user ON audit.audit_events(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_type ON audit.audit_events(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_events_recorded ON audit.audit_events(recorded_at DESC);
