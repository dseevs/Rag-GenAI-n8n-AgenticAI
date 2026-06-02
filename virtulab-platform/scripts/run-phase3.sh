#!/usr/bin/env bash
# VirtuLab Phase 3 — start Prometheus + Grafana
# Reference: docs/PHASE3.md
set -euo pipefail
ROOT="$(dirname "$0")/.."
cd "$ROOT/deploy"

docker compose -f docker-compose.yml -f docker-compose.phase3.yml up -d virtulab-prometheus virtulab-grafana

echo ""
echo "=== VirtuLab Phase 3 ==="
echo "Docs:       $ROOT/docs/PHASE3.md"
echo "Prometheus: http://localhost:9091"
echo "Grafana:    http://localhost:3001  (admin / admin)"
echo "Dashboard:  Phase 3 folder → Phase 3 — VirtuLab Services"
echo ""
echo "Backend JARs must be running: ./scripts/run-all.sh"
