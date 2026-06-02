#!/usr/bin/env bash
# VirtuLab Phase 7 — n8n automation
set -euo pipefail
ROOT="$(dirname "$0")/.."

cd "$ROOT/deploy"
echo "Starting n8n (UI http://localhost:5680 — admin / admin)..."
docker compose -f docker-compose.yml -f docker-compose.phase7.yml up -d virtulab-n8n

echo ""
echo "=== VirtuLab Phase 7 ==="
echo "Docs:       $ROOT/docs/PHASE7.md"
echo "Testing:    $ROOT/docs/TESTING_PLAN.md"
echo "n8n UI:     http://localhost:5680"
echo "Workflows:  $ROOT/virtulab-n8n/workflows/*.json (import in UI)"
echo "Kong proxy: http://localhost:8800/internal/n8n/  Header: X-API-Key: virtulab-n8n-dev-key"
echo ""
echo "Optional — notify n8n on progress (after N4 workflow active):"
echo "  export N8N_PROGRESS_WEBHOOK=http://localhost:5680/webhook/progress-live"
echo "  restart run-all.sh"
