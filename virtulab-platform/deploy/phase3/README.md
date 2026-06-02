# Phase 3 — Observability

All Phase 3 config lives here. See **[../../docs/PHASE3.md](../../docs/PHASE3.md)** for full instructions.

```bash
# From repo root
./scripts/run-phase3.sh
```

| Path | Purpose |
|------|---------|
| `prometheus/` | Scrape config + alerts |
| `grafana/` | Datasource + dashboard provisioning |

Compose overlay: `../docker-compose.phase3.yml`
