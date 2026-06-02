#!/usr/bin/env bash
# VirtuLab Phase 8 — audit-service + MCP server deps
set -euo pipefail
ROOT="$(dirname "$0")/.."

JAR="$ROOT/services/audit-service/target/audit-service-0.1.0-SNAPSHOT.jar"
if [[ ! -f "$JAR" ]]; then
  echo "Missing $JAR — run ./scripts/build.sh first"
  exit 1
fi

echo "Installing MCP server dependencies (once)..."
for dir in analytics ops; do
  if [[ ! -d "$ROOT/mcp-servers/$dir/node_modules" ]]; then
    (cd "$ROOT/mcp-servers/$dir" && npm install --silent)
  fi
done

echo "=== VirtuLab Phase 8 ==="
echo "Docs:      $ROOT/docs/PHASE8.md"
echo "MCP setup: $ROOT/docs/MCP_SETUP.md"
echo "Audit:     http://localhost:8096/api/v1/audit/events"
echo ""
echo "Starting audit-service (8096)... requires Kafka + prior progress/AI events"

java -jar "$JAR" &
echo "PID audit=$!"
wait
