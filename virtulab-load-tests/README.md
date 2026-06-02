# VirtuLab load tests (Phase 11)

JMeter test plans and bash fallbacks when JMeter is not installed.

## Prerequisites

- Stack running: `./scripts/run-all.sh` (+ phase5 for analytics-heavy)
- Keycloak on :9080

## Run

```bash
chmod +x *.sh scripts/*.sh
./run-smoke.sh              # 10 threads, health endpoints
./run-progress-storm.sh     # GraphQL recordProgress burst
./run-analytics-heavy.sh    # analytics-query load
./run-all.sh                # smoke + progress storm
```

Reports land in `reports/`.

Install JMeter 5.x for full HTML dashboards and 200-thread scenarios.

See `virtulab-platform/docs/JMETER_PLAN.md`.
