CREATE SCHEMA IF NOT EXISTS analytics;

CREATE TABLE IF NOT EXISTS analytics.dim_org (
    org_id      VARCHAR(128) PRIMARY KEY,
    tenant_id   VARCHAR(128) NOT NULL,
    org_name    VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS analytics.dim_user (
    user_id       VARCHAR(128) PRIMARY KEY,
    tenant_id     VARCHAR(128) NOT NULL,
    org_id        VARCHAR(128) NOT NULL REFERENCES analytics.dim_org(org_id),
    display_name  VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS analytics.fact_progress (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id          UUID NOT NULL UNIQUE,
    attempt_id        VARCHAR(128) NOT NULL,
    user_id           VARCHAR(128) NOT NULL,
    experiment_id     VARCHAR(128) NOT NULL,
    tenant_id         VARCHAR(128) NOT NULL DEFAULT 'tenant-dev',
    org_id            VARCHAR(128) NOT NULL DEFAULT 'org-dev',
    step_id           VARCHAR(128),
    time_spent_sec    INT,
    progress_tab      VARCHAR(64),
    progress_pct      INT,
    event_ts          TIMESTAMPTZ NOT NULL,
    ingested_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS analytics.fact_ai (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         VARCHAR(128) NOT NULL,
    experiment_id   VARCHAR(128),
    attempt_id      VARCHAR(128),
    event_type      VARCHAR(64) NOT NULL,
    source          VARCHAR(64),
    latency_ms      BIGINT,
    event_ts        TIMESTAMPTZ NOT NULL,
    ingested_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fact_progress_org ON analytics.fact_progress(org_id);
CREATE INDEX IF NOT EXISTS idx_fact_progress_experiment ON analytics.fact_progress(experiment_id);
CREATE INDEX IF NOT EXISTS idx_fact_progress_user ON analytics.fact_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_fact_ai_user ON analytics.fact_ai(user_id);
