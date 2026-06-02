CREATE SCHEMA IF NOT EXISTS quiz;

CREATE TABLE IF NOT EXISTS quiz.quiz_questions (
    id              VARCHAR(64) PRIMARY KEY,
    experiment_id   VARCHAR(128) NOT NULL,
    prompt          TEXT NOT NULL,
    correct_answer  VARCHAR(256) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS quiz.quiz_attempts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id      VARCHAR(128) NOT NULL,
    user_id         VARCHAR(128) NOT NULL,
    experiment_id   VARCHAR(128) NOT NULL,
    org_id          VARCHAR(128) NOT NULL DEFAULT 'org-dev',
    mode            VARCHAR(32) NOT NULL,
    score           INT NOT NULL,
    total_questions INT NOT NULL,
    correct_count   INT NOT NULL,
    answers_json    JSONB NOT NULL,
    submitted_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (attempt_id)
);

CREATE INDEX IF NOT EXISTS idx_quiz_attempts_org ON quiz.quiz_attempts(org_id);
CREATE INDEX IF NOT EXISTS idx_quiz_attempts_mode ON quiz.quiz_attempts(mode);

INSERT INTO quiz.quiz_questions (id, experiment_id, prompt, correct_answer, sort_order) VALUES
    ('q1', 'v1-chemistry', 'What pH indicates a neutral solution?', '7', 1),
    ('q2', 'v1-chemistry', 'Acids donate which ion?', 'H+', 2),
    ('q3', 'v1-chemistry', 'Bases accept protons or donate OH⁻ — true or false?', 'true', 3)
ON CONFLICT (id) DO NOTHING;
