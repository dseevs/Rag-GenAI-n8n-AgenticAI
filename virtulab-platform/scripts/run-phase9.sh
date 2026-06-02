#!/usr/bin/env bash
# VirtuLab Phase 9 — ml-scoring-service (8095)
set -euo pipefail
ROOT="$(dirname "$0")/.."
export DB_PORT="${DB_PORT:-5434}"
export KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-localhost:9095}"
export KEYCLOAK_ISSUER="${KEYCLOAK_ISSUER:-http://localhost:9080/realms/virtulab}"

JAR="$ROOT/services/ml-scoring-service/target/ml-scoring-service-0.1.0-SNAPSHOT.jar"
if [[ ! -f "$JAR" ]]; then
  echo "Missing $JAR — run ./scripts/build.sh first"
  exit 1
fi

echo "=== VirtuLab Phase 9 ==="
echo "Docs: $ROOT/docs/PHASE9.md"
echo "ML API: http://localhost:8095/api/v1/ml/model-vs-actual?orgId=org-dev"
echo ""
echo "Requires: docker compose, run-all.sh, run-phase5.sh (analytics facts)"
echo "Starting ml-scoring-service (8095)..."

java -jar "$JAR" &
echo "PID ml-scoring=$!"
wait
