# Local-only assets (not in Git)

These folders are **gitignored** on purpose. Copy or create them on your machine after cloning.

**First time?** Follow **[GETTING_STARTED.md](../GETTING_STARTED.md)** — it includes a minimal sample RAG file you can paste in minutes.

## 1. RAG corpus — `virtulab-platform/rag-corpus/`

Markdown sources ingested by `rag-ingest-service` (pgvector + embeddings).

Suggested layout:

```text
virtulab-platform/rag-corpus/
  v1-chemistry/
    experiment-guide.md
    safety-rules.md
    faq-student-questions.md
    ...
```

Each file should include YAML front matter where your ingest pipeline expects it, for example:

```yaml
---
experimentId: v1-chemistry
lang: en
docType: guide
---
```

Reindex after adding files (via API, n8n workflow **N3**, or your ops script).

## 2. Lab content — `virtulab-lab/content/`

Interactive lab definitions consumed by the lab shell (`virtulab-lab` on port 3002).

Mirror the same experiment id as the corpus (`v1-chemistry/`, etc.).

## 3. Internal docs — `virtulab-platform/docs/`

Phase runbooks (PHASE3–12), API collections, testing checklists, MCP setup, JMeter plans.

Keep these in a private wiki, encrypted drive, or separate private repo if your team needs them.

## 4. Environment secrets

| Location | What to set |
|----------|-------------|
| `virtulab-frontend/.env.local` | `AUTH_SECRET`, Keycloak client secret |
| Shell / Docker | `DB_PASSWORD`, `JWT_SECRET`, `RABBITMQ_PASSWORD` |
| Production K8s | Real `Secret` manifests (never commit plaintext) |
| Ollama / LLM | Model host URL, optional API keys for cloud models |
| n8n | Webhook URLs, SMTP, Slack tokens for workflows |

Use `virtulab-frontend/.env.example` as the template for the web app.

## 5. Optional: n8n credentials

Import workflows from `virtulab-platform/virtulab-n8n/workflows/` into your n8n instance, then configure credentials (Postgres, HTTP, Slack, etc.) inside the n8n UI — they are not stored in this repository.
