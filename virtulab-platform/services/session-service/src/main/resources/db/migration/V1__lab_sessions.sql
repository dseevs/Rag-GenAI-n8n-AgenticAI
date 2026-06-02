CREATE SCHEMA IF NOT EXISTS session;

CREATE TABLE session.lab_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id      VARCHAR(128) NOT NULL UNIQUE,
    experiment_id   VARCHAR(128) NOT NULL,
    user_id         VARCHAR(128) NOT NULL,
    tenant_id       VARCHAR(128) NOT NULL,
    org_id          VARCHAR(128) NOT NULL,
    mode            VARCHAR(32) NOT NULL,
    lang            VARCHAR(16) NOT NULL DEFAULT 'en',
    metadata_json   JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lab_sessions_user ON session.lab_sessions(user_id);
CREATE INDEX idx_lab_sessions_experiment ON session.lab_sessions(experiment_id);
