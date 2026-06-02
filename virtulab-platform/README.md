# VirtuLab Platform — Phase 1–3

> **Public repo note:** Internal runbooks under `docs/` are not published. Use **[GETTING_STARTED.md](../GETTING_STARTED.md)** and **[TECHNOLOGY_STACK.md](../TECHNOLOGY_STACK.md)** at the repo root; keep phase docs locally if you need them.

Reactive Spring Boot microservices: **auth**, **session**, **lab-events**, **graphql-gateway** + Postgres, Redis, Redpanda/Kafka, Kong, Keycloak.

**Phase 3:** Prometheus + Grafana — see [docs/PHASE3.md](docs/PHASE3.md).  
**Phase 4:** RAG + AI tutor + agents — see [docs/PHASE4.md](docs/PHASE4.md).  
**Phase 5:** Analytics ingest + JDBC reports — see [docs/PHASE5.md](docs/PHASE5.md).  
**Phase 6:** WebSocket live + RabbitMQ — see [docs/PHASE6.md](docs/PHASE6.md).  
**Phase 7:** n8n automation — see [docs/PHASE7.md](docs/PHASE7.md).  
**Phase 8:** MCP + audit — see [docs/PHASE8.md](docs/PHASE8.md), [docs/MCP_SETUP.md](docs/MCP_SETUP.md).  
**Phase 9:** ML scoring — see [docs/PHASE9.md](docs/PHASE9.md).  
**Phase 10:** Lab integration — see [docs/PHASE10.md](docs/PHASE10.md), [docs/LAB_INTEGRATION.md](docs/LAB_INTEGRATION.md).  
**Phase 11:** JMeter + profiling — see [docs/PHASE11.md](docs/PHASE11.md), [docs/JMETER_PLAN.md](docs/JMETER_PLAN.md).  
**Phase 12:** CI/K8s — see [docs/PHASE12.md](docs/PHASE12.md).  
**Full test checklist:** [docs/TESTING_PLAN.md](docs/TESTING_PLAN.md).

## Quick start

### 1. Start infrastructure

```bash
cd deploy
docker compose up -d
docker compose ps
```

### 2. Build & run services (Java 17+)

```bash
# from virtulab-platform root — requires Maven 3.9+
./scripts/build.sh

# three terminals, or use scripts/run-all.sh
./scripts/run-auth.sh
./scripts/run-session.sh
./scripts/run-events.sh
```

### 3. Postman

See [docs/API.md](docs/API.md).

### 4. Phase 3 — Observability

```bash
./scripts/run-phase3.sh   # Prometheus :9091, Grafana :3001
```

See [docs/PHASE3.md](docs/PHASE3.md).

### 5. Phase 4 — GenAI / RAG

```bash
./scripts/init-phase4-db.sh    # once (needs pgvector Postgres — see PHASE4.md)
./scripts/run-phase4.sh
```

See [docs/PHASE4.md](docs/PHASE4.md).

### 6. Phase 5 — Analytics

```bash
./scripts/run-phase5.sh
```

See [docs/PHASE5.md](docs/PHASE5.md).

### 7. Phase 6 — WebSocket + RabbitMQ

```bash
./scripts/run-phase6.sh
```

See [docs/PHASE6.md](docs/PHASE6.md).

### 8. Phase 7 — n8n

```bash
./scripts/run-phase7.sh
```

See [docs/PHASE7.md](docs/PHASE7.md). **Full testing:** [docs/TESTING_PLAN.md](docs/TESTING_PLAN.md).

### 9. Phase 8 — MCP + Audit

```bash
./scripts/run-phase8.sh
```

See [docs/PHASE8.md](docs/PHASE8.md) and [docs/MCP_SETUP.md](docs/MCP_SETUP.md).

### 10. Phase 9 — ML Scoring

```bash
./scripts/run-phase9.sh
```

See [docs/PHASE9.md](docs/PHASE9.md).

### 11. Phase 10 — Lab integration

```bash
./scripts/run-phase10.sh
# + virtulab-lab :3002 and virtulab-frontend :3000
```

See [docs/PHASE10.md](docs/PHASE10.md).

### 11. Phase 11 — Load tests

```bash
./scripts/run-phase11.sh   # or ../virtulab-load-tests/run-smoke.sh
```

See [docs/PHASE11.md](docs/PHASE11.md).

### 12. Phase 12 — CI / Docker / K8s

```bash
./scripts/run-phase12-check.sh
docker build -f deploy/docker/Dockerfile --build-arg SERVICE=auth-service -t virtulab/auth:dev .
```

See [docs/PHASE12.md](docs/PHASE12.md).

## Port map

See [docs/API.md](docs/API.md) — Postgres **5434**, Redis **6380**, Kafka **9095**, Kong **8800**.

## Project layout

```
virtulab-platform/
  contracts/                 shared DTOs + DevJwtService
  services/
    auth-service/            :8081
    session-service/         :8082
    lab-events-service/      :8083
  deploy/docker-compose.yml
  deploy/kong/kong.yml
```
