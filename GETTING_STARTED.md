# Getting started — run VirtuLab locally

Step-by-step guide for anyone cloning **[Rag-GenAI-n8n-AgenticAI](https://github.com/dseevs/Rag-GenAI-n8n-AgenticAI)**.

- **Architecture (HLD + LLD):** [ARCHITECTURE.md](ARCHITECTURE.md)  
- **Technologies explained:** [TECHNOLOGY_STACK.md](TECHNOLOGY_STACK.md)

---

## What is this project?

**VirtuLab** is an end-to-end **virtual STEM learning platform** that demonstrates how modern systems are built when you combine:

- A **web app** for students and instructors  
- **Microservices** for auth, sessions, lab telemetry, quizzes, and analytics  
- **RAG + GenAI** so an AI tutor answers from *your* course material (with citations)  
- **Agentic AI** for multi-step, tool-using workflows  
- **n8n** for scheduled jobs and webhook automation  
- **Event streaming**, **caching**, **SSO**, **observability**, and optional **Kubernetes** packaging  

It is designed as a **learning lab in code**: many industry tools appear together so you can study integration patterns in one repository—not only a single framework in isolation.

**Repository layout:**

| Folder | Role |
|--------|------|
| `virtulab-platform/` | Java/Spring microservices, Docker Compose, n8n workflows, MCP servers |
| `virtulab-frontend/` | Next.js shell — login, dashboard, AI Studio |
| `virtulab-lab/` | Next.js virtual chemistry lab UI |
| `virtulab-load-tests/` | JMeter smoke/load scripts |
| `local-setup/` | What you must add locally (corpus, lab content, secrets) |

---

## Who is this for?

- Developers learning **microservices**, **RAG**, **agents**, and **workflow automation**  
- Students or instructors running a **local demo**  
- Reviewers evaluating a **portfolio** full-stack + AI project  

---

## Requirements

| Software | Version | Used for |
|----------|---------|----------|
| **Docker** + Compose | recent | Postgres, Redis, Kafka, Keycloak, Kong, optional Ollama/n8n/RabbitMQ |
| **Java** | 17+ | Spring Boot services |
| **Maven** | 3.9+ | Build all backend JARs |
| **Node.js** | 20+ | Frontend and lab apps |
| **npm** | 10+ | Install JS dependencies |
| **Git** | any | Clone repo |
| **psql** (optional) | any | Phase 4 DB init script |
| **Ollama** (optional) | recent | Local LLM if not using Docker Ollama |

**Hardware:** 8 GB RAM minimum; 16 GB recommended when running Docker + Ollama + all services.

---

## Before you run — local-only files

These paths are **not on GitHub** (see [`.gitignore`](.gitignore) and [`local-setup/README.md`](local-setup/README.md)):

| Path | Purpose |
|------|---------|
| `virtulab-platform/rag-corpus/` | Markdown for RAG ingest |
| `virtulab-lab/content/` | Virtual lab scenarios |
| `virtulab-frontend/.env.local` | Auth secret and API URLs |

### Minimal RAG corpus (so AI features work)

Create at least one file:

```bash
mkdir -p virtulab-platform/rag-corpus/v1-chemistry
cat > virtulab-platform/rag-corpus/v1-chemistry/getting-started.md << 'EOF'
---
experimentId: v1-chemistry
lang: en
docType: guide
---

# VirtuLab sample corpus

The litmus test turns red in acid and blue in base.
Always wear goggles in the virtual lab.
EOF
```

You can add more `.md` files under `v1-chemistry/` later.

---

## Run paths

Choose **Path A** (core platform, ~15 min) or **Path B** (full stack with AI and extras).

---

## Path A — Core platform (recommended first)

### Step 1 — Clone

```bash
git clone git@github.com:dseevs/Rag-GenAI-n8n-AgenticAI.git
cd Rag-GenAI-n8n-AgenticAI
```

### Step 2 — Start infrastructure

```bash
cd virtulab-platform/deploy
docker compose up -d
docker compose ps
```

Wait until Postgres, Redis, Redpanda, Kong, and Keycloak are healthy.

### Step 3 — Build backend

```bash
cd ../..   # repo root
cd virtulab-platform
./scripts/build.sh
```

First build downloads Maven dependencies and may take several minutes.

### Step 4 — Start core microservices

In a **dedicated terminal** (keeps running):

```bash
cd virtulab-platform
./scripts/run-all.sh
```

Starts: **auth** `8081`, **session** `8082`, **lab-events** `8083`, **graphql** `8097`.

### Step 5 — Frontend

In a **second terminal**:

```bash
cd virtulab-frontend
cp .env.example .env.local
```

Edit `.env.local`:

```bash
# Generate a secret:
openssl rand -base64 32
# Paste result into AUTH_SECRET=
```

```bash
npm install
npm run dev
```

### Step 6 — Open the app

| URL | What |
|-----|------|
| http://localhost:3000 | Platform (login, dashboard) |
| http://localhost:9080 | Keycloak admin (`admin` / `admin`) |

**Demo users** (Keycloak realm `virtulab`):

| User | Password | Role idea |
|------|----------|-----------|
| `student1` | `password` | Student |
| `dev1` | `password` | Developer |
| `admin1` | `password` | Admin |

Sign in at http://localhost:3000 → you should reach the dashboard and see GraphQL `me` data.

### Step 7 — Verify health

```bash
curl -s http://localhost:8081/actuator/health
curl -s http://localhost:8097/actuator/health
```

---

## Path B — Full stack (AI, analytics, realtime, automation)

Complete **Path A** first, then add phases below. Each phase can run in its **own terminal** unless noted.

### B1 — Phase 4 — RAG, AI tutor, agents (GenAI)

```bash
# Once: enable pgvector in Postgres
cd virtulab-platform
./scripts/init-phase4-db.sh

# Pull Ollama models (host or container)
ollama pull nomic-embed-text
ollama pull llama3.2

# Start AI plane (new terminal)
./scripts/run-phase4.sh
```

Services: **rag-ingest** `8086`, **ai-tutor** `8084`, **agent-orchestrator** `8085`.

**Reindex corpus** (after services are up):

```bash
# Example — use a valid JWT from login or dev tooling
curl -X POST http://localhost:8086/api/v1/rag/reindex \
  -H "Authorization: Bearer <access_token>"
```

Or use **AI Studio** in the frontend at http://localhost:3000/ai-studio.

### B2 — Phase 5 — Analytics

```bash
./scripts/run-phase5.sh
```

**analytics-ingest** `8089`, **analytics-query** `8091`. Requires Kafka and lab-events producing events.

### B3 — Phase 6 — WebSocket + notifications

```bash
./scripts/run-phase6.sh
```

Starts RabbitMQ (`5673`, UI `15673`) + **websocket-gateway** `8090` + **notification** `8092`.

### B4 — Phase 7 — n8n automation

```bash
./scripts/run-phase7.sh
```

Open http://localhost:5680 — login `admin` / `admin`. Import workflows from `virtulab-platform/virtulab-n8n/workflows/`.

### B5 — Phase 8 — Audit + MCP

```bash
./scripts/run-phase8.sh
```

**audit-service** `8096`. MCP sample servers under `virtulab-platform/mcp-servers/`.

### B6 — Phase 9 — ML scoring

```bash
./scripts/run-phase9.sh
```

**ml-scoring** `8095` (requires analytics data from Phase 5).

### B7 — Phase 10 — Virtual lab UI

```bash
# Terminal: lab app
cd virtulab-lab
npm install
npm run dev
```

Open http://localhost:3002. Add lab content under `virtulab-lab/content/` (local-only; see `local-setup/`).

### B8 — Phase 3 — Observability (optional)

```bash
cd virtulab-platform
./scripts/run-phase3.sh
```

| URL | Tool |
|-----|------|
| http://localhost:9091 | Prometheus |
| http://localhost:3001 | Grafana (`admin` / `admin`) |

### B9 — Phase 11 — Load tests (optional)

```bash
cd virtulab-load-tests
chmod +x *.sh
./run-smoke.sh
```

### B10 — Phase 12 — Docker / K8s (optional)

```bash
cd virtulab-platform
./scripts/run-phase12-check.sh
```

See `virtulab-platform/deploy/k8s/` and `.github/workflows/` for CI/CD patterns.

---

## Port reference

### Infrastructure (Docker)

| Port | Service |
|------|---------|
| 5434 | PostgreSQL (+ pgvector) |
| 6380 | Redis |
| 9095 | Kafka (Redpanda external) |
| 8800 | Kong proxy |
| 8801 | Kong admin |
| 9080 | Keycloak |
| 11434 | Ollama (host or phase4 compose) |
| 5673 | RabbitMQ AMQP |
| 15673 | RabbitMQ management UI |
| 5680 | n8n |
| 9091 | Prometheus |
| 3001 | Grafana |

### Microservices (JARs on host)

| Port | Service |
|------|---------|
| 8081 | auth-service |
| 8082 | session-service |
| 8083 | lab-events-service |
| 8084 | ai-tutor-service |
| 8085 | agent-orchestrator-service |
| 8086 | rag-ingest-service |
| 8088 | quiz-service |
| 8089 | analytics-ingest-service |
| 8090 | websocket-gateway-service |
| 8091 | analytics-query-service |
| 8092 | notification-service |
| 8095 | ml-scoring-service |
| 8096 | audit-service |
| 8097 | graphql-gateway-service |

### Frontends

| Port | App |
|------|-----|
| 3000 | virtulab-frontend |
| 3002 | virtulab-lab |

---

## Stopping everything

```bash
# Stop Java services (from virtulab-platform)
./scripts/stop-all.sh
# Or Ctrl+C in each terminal running run-*.sh

# Stop Docker
cd virtulab-platform/deploy
docker compose down
# With overlays, e.g.:
# docker compose -f docker-compose.yml -f docker-compose.phase6.yml -f docker-compose.phase7.yml down
```

---

## Common problems

| Symptom | Fix |
|---------|-----|
| Port already in use | Check `ss -tlnp \| grep <port>`; stop conflicting process or change compose ports |
| Keycloak login fails | `docker compose restart virtulab-keycloak`; ensure `AUTH_KEYCLOAK_ISSUER` matches `http://localhost:9080/realms/virtulab` |
| `Missing ...jar` | Run `./scripts/build.sh` |
| AI returns errors | Ensure Ollama is running and models are pulled; check `OLLAMA_BASE_URL` |
| RAG empty answers | Create `rag-corpus/`, run reindex, confirm `init-phase4-db.sh` succeeded |
| 401 on API calls | Use Bearer token from Keycloak; services expect JWT issuer realm `virtulab` |

---

## Default credentials (development only)

| System | User | Password |
|--------|------|----------|
| Keycloak admin | `admin` | `admin` |
| Keycloak demo users | `student1` / `dev1` / `admin1` | `password` |
| Postgres | `virtulab` | `virtulab` |
| RabbitMQ | `virtulab` | `virtulab` |
| n8n | `admin` | `admin` |
| Grafana | `admin` | `admin` |

**Rotate all of these before any shared or production deployment.**

---

## Next steps

- **[TECHNOLOGY_STACK.md](TECHNOLOGY_STACK.md)** — deep dive on every tool and concept  
- **[local-setup/README.md](local-setup/README.md)** — private corpus and lab content  
- **[GITHUB_SETUP.md](GITHUB_SETUP.md)** — clone, push, and authentication  
