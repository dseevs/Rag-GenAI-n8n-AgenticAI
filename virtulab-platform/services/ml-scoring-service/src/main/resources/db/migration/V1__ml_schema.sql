CREATE SCHEMA IF NOT EXISTS ml;

CREATE TABLE IF NOT EXISTS ml.model_registry (
    model_version   VARCHAR(64) PRIMARY KEY,
    description     VARCHAR(512) NOT NULL,
    weights_json    JSONB NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    registered_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS ml.ml_predictions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id      VARCHAR(128) NOT NULL,
    user_id         VARCHAR(128),
    experiment_id   VARCHAR(128),
    org_id          VARCHAR(128) NOT NULL DEFAULT 'org-dev',
    model_version   VARCHAR(64) NOT NULL REFERENCES ml.model_registry(model_version),
    score           DOUBLE PRECISION NOT NULL,
    features_json   JSONB NOT NULL,
    predicted_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ml_predictions_attempt ON ml.ml_predictions(attempt_id, predicted_at DESC);
CREATE INDEX IF NOT EXISTS idx_ml_predictions_org ON ml.ml_predictions(org_id);

INSERT INTO ml.model_registry (model_version, description, weights_json, active)
VALUES (
    'v1-logistic-heuristic',
    'Heuristic completion model: progress + AI usage (Phase 9 MVP)',
    '{"bias": -0.35, "maxProgressPct": 0.045, "aiEventCount": 0.18, "progressEventCount": 0.05}'::jsonb,
    TRUE
)
ON CONFLICT (model_version) DO NOTHING;
