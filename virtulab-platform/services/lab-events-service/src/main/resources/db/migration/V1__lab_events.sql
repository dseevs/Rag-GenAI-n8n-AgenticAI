CREATE SCHEMA IF NOT EXISTS events;

CREATE TABLE events.lab_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id      VARCHAR(128) NOT NULL,
    user_id         VARCHAR(128) NOT NULL,
    experiment_id   VARCHAR(128) NOT NULL,
    step_id         VARCHAR(128) NOT NULL,
    time_spent_sec  INTEGER,
    progress_json   JSONB NOT NULL,
    event_type      VARCHAR(32) NOT NULL DEFAULT 'PROGRESS',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lab_events_attempt ON events.lab_events(attempt_id);
CREATE INDEX idx_lab_events_created ON events.lab_events(created_at);
