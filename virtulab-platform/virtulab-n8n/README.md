# VirtuLab n8n workflows (Phase 7)

Import JSON files from `workflows/` into n8n UI (http://localhost:5680).

| ID | File | Trigger |
|----|------|---------|
| N1 | `N1-daily-org-report.json` | Cron daily 06:00 |
| N2 | `N2-dlq-alert-webhook.json` | Webhook `/webhook/dlq-alert` |
| N3 | `N3-rag-reindex-weekly.json` | Cron weekly |
| N4 | `N4-progress-live-webhook.json` | Webhook `/webhook/progress-live` |
| N6 | `N6-circuit-breaker-webhook.json` | Webhook `/webhook/cb-open` |

After import: activate workflow → copy Production webhook URL.

See `../docs/PHASE7.md` and `../docs/TESTING_PLAN.md`.
