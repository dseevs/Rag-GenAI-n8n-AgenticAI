#!/usr/bin/env bash
# VirtuLab Phase 11 — run load tests (requires stack)
set -euo pipefail
ROOT="$(dirname "$0")/.."
LOAD="$ROOT/../virtulab-load-tests"

if [[ ! -d "$LOAD" ]]; then
  echo "Missing $LOAD"
  exit 1
fi

echo "=== VirtuLab Phase 11 ==="
echo "Docs: $ROOT/docs/PHASE11.md"
echo "Ensure ./scripts/run-all.sh is running"
echo ""

chmod +x "$LOAD"/*.sh "$LOAD"/scripts/*.sh 2>/dev/null || true
"$LOAD/run-smoke.sh"
echo ""
"$LOAD/run-progress-storm.sh"
