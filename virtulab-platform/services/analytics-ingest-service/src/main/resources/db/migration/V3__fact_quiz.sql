CREATE TABLE IF NOT EXISTS analytics.fact_quiz (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id      VARCHAR(128) NOT NULL,
    user_id         VARCHAR(128) NOT NULL,
    experiment_id   VARCHAR(128),
    mode            VARCHAR(32) NOT NULL,
    score           INT NOT NULL,
    total_questions INT NOT NULL,
    correct_count   INT NOT NULL,
    event_ts        TIMESTAMPTZ NOT NULL,
    ingested_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fact_quiz_user ON analytics.fact_quiz(user_id);
