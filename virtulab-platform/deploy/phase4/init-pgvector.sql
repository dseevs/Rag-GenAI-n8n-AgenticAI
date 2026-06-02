-- VirtuLab Phase 4 — run once against Postgres (port 5434)
-- psql -h localhost -p 5434 -U virtulab -d virtulab -f deploy/phase4/init-pgvector.sql

CREATE EXTENSION IF NOT EXISTS vector;
CREATE SCHEMA IF NOT EXISTS rag;

CREATE TABLE IF NOT EXISTS rag.corpus_version (
    version      INT PRIMARY KEY,
    active       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rag.knowledge_chunks (
    id             UUID PRIMARY KEY,
    content        TEXT NOT NULL,
    embedding      vector(768),
    metadata       JSONB NOT NULL DEFAULT '{}',
    corpus_version INT NOT NULL REFERENCES rag.corpus_version(version)
);

CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_version
    ON rag.knowledge_chunks (corpus_version);

CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_experiment
    ON rag.knowledge_chunks ((metadata->>'experimentId'));

INSERT INTO rag.corpus_version (version, active)
VALUES (0, TRUE)
ON CONFLICT (version) DO NOTHING;

CREATE SCHEMA IF NOT EXISTS agents;

CREATE TABLE IF NOT EXISTS agents.agent_runs (
    id            UUID PRIMARY KEY,
    attempt_id    VARCHAR(128),
    agent_type    VARCHAR(64) NOT NULL,
    status        VARCHAR(32) NOT NULL,
    input_json    JSONB,
    output_json   JSONB,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS agents.agent_steps (
    id            UUID PRIMARY KEY,
    run_id        UUID NOT NULL REFERENCES agents.agent_runs(id),
    step_index    INT NOT NULL,
    tool_name     VARCHAR(64),
    tool_input    TEXT,
    tool_output   TEXT,
    llm_message   TEXT
);
