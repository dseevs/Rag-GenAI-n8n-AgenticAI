# Technology stack — concepts and how VirtuLab uses them

This document explains **every major technology** in the repository: what it is, what problem it solves, and where you will see it in the codebase. The goal is to study **many real-world patterns in one integrated system**—microservices, data, messaging, security, AI, automation, and operations.

For **system diagrams, API contracts, database schemas, and request flows**, see **[ARCHITECTURE.md](ARCHITECTURE.md)** (HLD + LLD).

---

## Map: learning goals by area

| Area | Technologies | Concepts you practice |
|------|----------------|----------------------|
| Frontend | Next.js, React, TypeScript, Tailwind, Auth.js, Redux Saga | SSR/SPA, OIDC login, client state, API proxies |
| Backend | Spring Boot 3, WebFlux, Maven | Reactive microservices, REST/GraphQL, validation, actuator |
| Security | Keycloak, JWT, OAuth2 Resource Server, Kong | SSO, RBAC, API gateway, token propagation |
| Data | PostgreSQL, pgvector, Redis, Flyway | Relational data, vector search, caching, migrations |
| Messaging | Redpanda/Kafka, RabbitMQ | Event-driven architecture, async notifications |
| AI | Ollama, RAG, agents, Temporal (optional) | Embeddings, retrieval, LLM calls, durable jobs |
| Automation | n8n | Cron, webhooks, low-code integrations |
| Observability | Prometheus, Grafana, Micrometer | Metrics, dashboards, health checks |
| Quality | JMeter, Resilience4j | Load testing, circuit breakers |
| Platform | Docker, Kubernetes, GitHub Actions | Containers, orchestration, CI/CD |
| Extensibility | MCP servers | Tool protocols for AI assistants |

---

## Frontend layer

### Next.js 16

**What it is:** A React framework with file-based routing, server and client components, and built-in API routes.

**Why here:** Powers `virtulab-frontend` (port **3000**) and `virtulab-lab` (port **3002**). Next.js gives a production-shaped structure: layouts, `/app` routes, and server-side API proxies to backend services (e.g. `/api/ai/*` → RAG/tutor/agent URLs).

**Concept:** Full-stack JavaScript—UI and lightweight BFF (backend-for-frontend) in one repo folder.

**Where:** `virtulab-frontend/src/app/`, `virtulab-lab/src/app/`.

---

### React 19

**What it is:** UI library for component-based interfaces.

**Why here:** All pages and interactive lab UI are React components.

**Concept:** Declarative UI, hooks, composition—foundation under Next.js.

---

### TypeScript 5

**What it is:** Typed superset of JavaScript.

**Why here:** Catches API contract mistakes at build time; used across both Next apps.

**Concept:** Safer frontends at scale—especially when calling GraphQL and REST from many screens.

---

### Tailwind CSS 4

**What it is:** Utility-first CSS framework.

**Why here:** Rapid, consistent styling for dashboard, login, and AI Studio without large custom CSS files.

**Concept:** Design tokens via classes; good for demos and portfolios.

---

### Auth.js (NextAuth v5)

**What it is:** Authentication library for Next.js with OAuth/OIDC providers.

**Why here:** Frontend login uses **Keycloak** as OIDC provider (`virtulab-web` client). Session cookies protect routes; server components read the session.

**Concept:** **Authorization Code + PKCE** flow—industry standard for browser apps; secrets stay server-side (`AUTH_SECRET` in `.env.local`).

**Config:** `virtulab-frontend/.env.example` — `AUTH_KEYCLOAK_ISSUER`, `AUTH_KEYCLOAK_ID`.

---

### Redux Toolkit + Redux Saga

**What it is:** Predictable state container (RTK) + side-effect middleware (Saga).

**Why here:** Platform shell uses sagas for async flows (e.g. loading user/lab state) where multiple steps and retries matter.

**Concept:** Separating **UI state** from **async orchestration**—alternative to React Query for complex sequences.

---

### Zod

**What it is:** Schema validation for TypeScript/JavaScript.

**Why here:** Validates API payloads and form input at runtime.

**Concept:** Same idea as Bean Validation on the backend—fail fast on bad data.

---

## Backend layer (Java / Spring)

### Java 17

**What it is:** LTS language runtime for all microservices.

**Why here:** Records, pattern matching, modern APIs—stable enterprise choice.

---

### Maven 3.9+

**What it is:** Build and dependency management for Java multi-module projects.

**Why here:** Root `virtulab-platform/pom.xml` aggregates `contracts` + 14 `services/*`. `./scripts/build.sh` compiles all JARs.

**Concept:** Monorepo build orchestration—shared BOM versions (Spring Boot 3.3.6).

---

### Spring Boot 3.3

**What it is:** Opinionated framework for production Java services—auto-config, embedded Netty/Tomcat, starters.

**Why here:** Every microservice is a Spring Boot app with `application.yml`, actuator, and security.

**Concept:** **Microservices** as independently deployable JARs sharing conventions.

---

### Spring WebFlux (reactive stack)

**What it is:** Non-blocking web layer on Project Reactor (`Mono` / `Flux`).

**Why here:** Services use `spring-boot-starter-webflux` for high concurrency on I/O-bound work (HTTP to Ollama, DB, Kafka).

**Concept:** **Reactive programming**—fewer threads blocked waiting on network; fits gateway and AI proxy patterns.

**Note:** Not every operation is fully reactive end-to-end, but the stack choice teaches reactive Spring.

---

### Spring Security + OAuth2 Resource Server

**What it is:** Authentication/authorization framework; resource server validates JWTs from Keycloak.

**Why here:** Microservices trust bearer tokens; issuer URI `http://localhost:9080/realms/virtulab`. Role checks (`student`, `instructor`, `admin`) guard RAG reindex, admin APIs, etc.

**Concept:** **Zero-trust between services**—caller identity travels as signed JWT, not implicit trust.

---

### JJWT / custom tokens (auth-service)

**What it is:** Issuing platform JWTs after Keycloak login where needed for service-to-service or lab events.

**Why here:** `auth-service` bridges Keycloak identity and platform-specific claims.

**Concept:** **Federation**—external IdP + internal token shape for legacy/fast paths.

---

### Spring Data / JDBC (reactive drivers)

**What it is:** Database access abstractions.

**Why here:** Sessions, vectors, analytics facts, quiz attempts stored in **PostgreSQL**.

**Concept:** **Database-per-service** pattern (schemas can share one Postgres instance in dev, split in prod).

---

### Flyway (SQL migrations)

**What it is:** Versioned database migrations (`V1__...sql`).

**Why here:** Each service owns tables under its schema; reproducible schema on fresh Postgres.

**Concept:** **Infrastructure as code** for database shape.

---

### Resilience4j

**What it is:** Fault tolerance library (circuit breaker, retry, rate limit).

**Why here:** `ai-tutor-service` wraps **Ollama** calls with a circuit breaker—if the LLM is down, fail fast instead of hanging the platform.

**Concept:** **Defensive integration** with unreliable AI backends.

---

### Springdoc OpenAPI (WebFlux UI)

**What it is:** Swagger/OpenAPI docs for REST endpoints.

**Why here:** Explore APIs on services like lab-events during development.

---

### Shared `contracts` module

**What it is:** Common DTOs, JWT helpers, shared types.

**Why here:** Avoid duplicating payload shapes across 14 services.

**Concept:** **Shared kernel**—keep it small to prevent tight coupling.

---

## API and integration styles

### GraphQL (graphql-gateway-service :8097)

**What it is:** Query language where clients request exactly the fields they need; one endpoint aggregates backend calls.

**Why here:** Frontend dashboard uses GraphQL `me` and progress mutations instead of many REST round-trips.

**Concept:** **BFF aggregation**—gateway composes auth/session/events data.

**Where:** `virtulab-platform/services/graphql-gateway-service/`.

---

### REST

**What it is:** Resource-oriented HTTP APIs with JSON bodies.

**Why here:** RAG (`/api/v1/rag/*`), AI tutor, agents, analytics, audit, quiz—each service exposes REST for clarity and tooling (curl, Postman).

**Concept:** **Service boundaries**—each domain owns its URL namespace.

---

### Kong API Gateway (:8800)

**What it is:** Lighter-weight API gateway (routes, plugins, rate limiting).

**Why here:** Declarative `deploy/kong/kong.yml` can front core services—teaches **edge routing** in front of microservices.

**Concept:** Single public entry vs. clients calling 10 ports directly.

---

## Identity and access

### Keycloak 26

**What it is:** Open-source Identity and Access Management (OIDC/SAML, realms, clients, roles).

**Why here:** Realm `virtulab` imported from `deploy/keycloak/realm-virtulab.json` with demo users and roles.

**Concept:** **Central SSO**—one login for frontend and token issuance for APIs.

**Dev URL:** http://localhost:9080 — admin `admin`/`admin`.

---

### OIDC / OAuth2 / PKCE

**What it is:** Standard protocols for delegated authentication; PKCE secures public clients (SPAs) without a client secret.

**Why here:** `virtulab-web` client + Auth.js on the frontend.

**Concept:** Industry pattern used by Google, Azure AD, Auth0—Keycloak is the self-hosted equivalent.

---

## Data stores

### PostgreSQL 16

**What it is:** Relational database—ACID transactions, SQL, indexes.

**Why here:** System of record for users, sessions, lab events, analytics aggregates, quiz data, audit.

**Dev port:** **5434** (non-default to avoid clashes with local Postgres).

---

### pgvector

**What it is:** Postgres extension for **vector similarity search** (embeddings).

**Why here:** RAG stores chunk embeddings; retrieval uses nearest-neighbor search before prompting the LLM.

**Concept:** **Vector database** pattern without a separate DB in dev—production might use dedicated vector store.

**Setup:** `./scripts/init-phase4-db.sh` enables extension.

---

### Redis 7

**What it is:** In-memory data store—cache, pub/sub, session material.

**Why here:** Session service and WebSocket gateway use Redis for fast reads and live fan-out.

**Dev port:** **6380**.

**Concept:** **Cache-aside** and **ephemeral state**—reduce Postgres load and enable realtime.

---

## Messaging and events

### Apache Kafka API (Redpanda)

**What it is:** Distributed **event log**—producers write topics; consumers read at their pace.

**Why here:** Redpanda (`virtulab-redpanda`) implements Kafka protocol on port **9095**. Lab events, analytics ingest, AI events flow as streams.

**Concept:** **Event-driven architecture**—decouple producers (lab-events) from consumers (analytics-ingest, audit).

**Topics example:** progress events, `lab.ai.v1` for AI plane.

---

### RabbitMQ

**What it is:** Message **broker** with queues, routing, acknowledgements—great for task dispatch.

**Why here:** Phase 6—notifications and async work; management UI on **15673**.

**Concept:** **Kafka vs RabbitMQ**—log/replay vs queue/worker; both appear so you can compare.

---

## AI, RAG, and agents

### Ollama

**What it is:** Local runtime to pull and serve open models (LLM + embeddings).

**Why here:** Default `OLLAMA_BASE_URL=http://localhost:11434` with models `nomic-embed-text` (embeddings) and `llama3.2` (chat).

**Concept:** **Local-first GenAI**—no cloud API key required for demos; swap for OpenAI-compatible endpoints in config.

---

### RAG (Retrieval-Augmented Generation)

**What it is:** Pipeline: chunk documents → embed → store vectors → at query time retrieve top-k chunks → inject into LLM prompt.

**Why here:** `rag-ingest-service` loads markdown from `rag-corpus/`, indexes into pgvector; `ai-tutor-service` retrieves citations before answering.

**Concept:** Grounding reduces **hallucinations** on factual lab procedures.

**Where:** `virtulab-platform/services/rag-ingest-service/`, corpus path in `application.yml`.

---

### Embeddings

**What it is:** Dense numeric vectors representing text meaning; similar text → similar vectors.

**Why here:** 768-dimensional vectors from `nomic-embed-text`; similarity search in Postgres.

**Concept:** Foundation of semantic search—not keyword match only.

---

### ai-tutor-service

**What it is:** GenAI API—ask questions with retrieved context, stream or return citations.

**Why here:** Powers **AI Studio** in the frontend.

**Concept:** **LLM application layer**—prompt construction, safety, logging.

---

### agent-orchestrator-service

**What it is:** Coordinates **multi-step agent** workflows—calling tutor, quiz, or platform tools in a plan.

**Why here:** Demonstrates **agentic AI** beyond single-shot chat.

**Concept:** Autonomy with **tools** and orchestration (plan → act → observe).

---

### Temporal (optional)

**What it is:** Durable workflow engine—long-running jobs survive restarts.

**Why here:** RAG reindex can use Temporal workflows (`TEMPORAL_ENABLED=true`) instead of fire-and-forget jobs.

**Concept:** **Reliable async** for production ingest pipelines.

**Compose:** `deploy/docker-compose.temporal.yml`.

---

### MCP (Model Context Protocol) servers

**What it is:** Standard way for AI assistants to call **tools** (read metrics, ops actions).

**Why here:** `mcp-servers/analytics` and `mcp-servers/ops` (Node.js) sample integrations for Phase 8.

**Concept:** Extending LLM apps with **structured tool access**—related to agents but protocol-standardized.

---

## Automation

### n8n

**What it is:** Fair-code **workflow automation**—visual editor, cron, webhooks, 400+ integrations.

**Why here:** JSON workflows in `virtulab-n8n/workflows/`—daily reports, DLQ alerts, weekly RAG reindex, progress webhooks.

**Concept:** **Low-code ops**—engineers define automation without redeploying Java for every integration.

**UI:** http://localhost:5680.

| Workflow | Purpose |
|----------|---------|
| N1 | Daily org report (cron) |
| N2 | DLQ alert webhook |
| N3 | Weekly RAG reindex |
| N4 | Progress live webhook |
| N6 | Circuit breaker open alert |

---

## Analytics and ML

### analytics-ingest-service + analytics-query-service

**What it is:** Ingest pipeline reads Kafka events into analytical tables; query service serves reports/JDBC-style APIs.

**Why here:** Instructor/org dashboards—progress, engagement.

**Concept:** **CQRS-lite**—write path (ingest) separated from read path (query).

---

### ml-scoring-service

**What it is:** Compares model predictions vs actual outcomes (e.g. risk scores vs results).

**Why here:** Phase 9 teaches **ML ops hook**—batch or API scoring over analytics facts.

**Scripts:** `virtulab-platform/virtulab-ml/` for model-related assets.

---

### quiz-service

**What it is:** Quiz attempts and scoring API.

**Why here:** Structured assessment alongside free-form AI tutor—agents can interact with quizzes.

---

## Realtime and notifications

### websocket-gateway-service

**What it is:** WebSocket endpoint for live updates to browsers.

**Why here:** `ws://localhost:8090/api/v1/ws/live?token=...` pushes progress events.

**Concept:** **Realtime UX** over HTTP long-polling alternative.

---

### notification-service

**What it is:** Consumes RabbitMQ messages and triggers notification channels (extensible to email/Slack).

**Why here:** Shows async **user notifications** decoupled from lab-events.

---

### audit-service

**What it is:** Immutable-style **audit trail** of important actions from Kafka.

**Why here:** Compliance and debugging—who did what, when.

---

## Observability and resilience

### Micrometer + Spring Actuator

**What it is:** Metrics and health endpoints (`/actuator/health`, `/actuator/prometheus`).

**Why here:** Every service exposes health for Docker/K8s probes and Prometheus scraping.

---

### Prometheus

**What it is:** Time-series metrics database—scrapes HTTP endpoints, stores samples, alerts.

**Why here:** Phase 3 compose—port **9091**.

**Concept:** **SRE fundamentals**—RED/USE metrics, alerting rules in `deploy/phase3/prometheus/`.

---

### Grafana

**What it is:** Dashboards on top of Prometheus (and other sources).

**Why here:** Pre-provisioned dashboard `virtulab-phase3-services.json`—port **3001**.

---

## Testing and delivery

### JMeter

**What it is:** Load testing tool—simulates many users hitting HTTP/GraphQL.

**Why here:** `virtulab-load-tests/` smoke and analytics-heavy plans; `run-smoke.sh` for quick checks.

**Concept:** **Performance validation** before production.

---

### Docker & Docker Compose

**What it is:** Container packaging and multi-container dev stacks.

**Why here:** `deploy/docker-compose.yml` + phase overlays (Ollama, RabbitMQ, n8n, Prometheus).

**Concept:** **Dev/prod parity**—same images locally and in CI.

---

### Kubernetes manifests

**What it is:** Declarative deployment to K8s clusters.

**Why here:** `deploy/k8s/` — Deployments, Services, ConfigMaps; `secret.yaml` is gitignored (use examples locally).

**Concept:** **Orchestration** at scale—replicas, rolling updates, service discovery.

---

### GitHub Actions

**What it is:** CI/CD on GitHub events.

**Why here:** `.github/workflows/docker-publish.yml` builds service images.

**Concept:** **Pipeline as code**—test and publish on every push.

---

## Cross-cutting architecture patterns

| Pattern | How VirtuLab demonstrates it |
|---------|------------------------------|
| **Microservices** | 14 Spring Boot services, single responsibility each |
| **API Gateway** | Kong + GraphQL gateway |
| **SSO** | Keycloak + JWT resource servers |
| **Event-driven** | Kafka topics between lab, analytics, audit, AI |
| **Caching** | Redis for session and live data |
| **RAG** | pgvector + Ollama + ingest pipeline |
| **Agents** | Orchestrator calling tutor and platform APIs |
| **Workflow automation** | n8n cron and webhooks |
| **Observability** | Prometheus + Grafana + actuator |
| **Defense in depth** | Resilience4j, role checks, gitignored secrets |

---

## Service catalog (quick reference)

| Service | Port | Primary tech focus |
|---------|------|-------------------|
| auth-service | 8081 | JWT, Keycloak, Redis |
| session-service | 8082 | Sessions, Redis |
| lab-events-service | 8083 | Kafka, RabbitMQ, events |
| ai-tutor-service | 8084 | RAG + Ollama chat |
| agent-orchestrator-service | 8085 | Agentic workflows |
| rag-ingest-service | 8086 | Embeddings, pgvector, Temporal |
| quiz-service | 8088 | Assessments |
| analytics-ingest-service | 8089 | Kafka → Postgres |
| websocket-gateway-service | 8090 | WebSockets, Redis |
| analytics-query-service | 8091 | Read models / reports |
| notification-service | 8092 | RabbitMQ consumers |
| ml-scoring-service | 8095 | ML evaluation API |
| audit-service | 8096 | Audit trail |
| graphql-gateway-service | 8097 | GraphQL aggregation |

---

## Suggested learning order

1. Run **Path A** in [GETTING_STARTED.md](GETTING_STARTED.md) — Docker, core services, frontend, Keycloak.  
2. Read **GraphQL** flow: login → dashboard `me` query.  
3. Add **RAG corpus** → Phase 4 → AI Studio.  
4. Enable **Kafka analytics** (Phase 5) and watch events flow.  
5. Add **WebSocket** (Phase 6) and **n8n** (Phase 7).  
6. Explore **Prometheus/Grafana** (Phase 3) and **JMeter** (load-tests).  
7. Skim **K8s** and **GitHub Actions** when ready for deployment topics.

---

## Further reading (external)

- [Spring Boot docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)  
- [Next.js docs](https://nextjs.org/docs)  
- [Keycloak docs](https://www.keycloak.org/documentation)  
- [pgvector](https://github.com/pgvector/pgvector)  
- [Ollama](https://ollama.com/)  
- [n8n docs](https://docs.n8n.io/)  
- [Temporal](https://docs.temporal.io/)  
- [Model Context Protocol](https://modelcontextprotocol.io/)  
