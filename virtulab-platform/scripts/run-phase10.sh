#!/usr/bin/env bash
# VirtuLab Phase 10 — quiz-service (8088)
set -euo pipefail
ROOT="$(dirname "$0")/.."
export DB_PORT="${DB_PORT:-5434}"
export KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-localhost:9095}"
export KEYCLOAK_ISSUER="${KEYCLOAK_ISSUER:-http://localhost:9080/realms/virtulab}"

JAR="$ROOT/services/quiz-service/target/quiz-service-0.1.0-SNAPSHOT.jar"
if [[ ! -f "$JAR" ]]; then
  echo "Missing $JAR — run ./scripts/build.sh first"
  exit 1
fi

echo "=== VirtuLab Phase 10 ==="
echo "Docs:       $ROOT/docs/PHASE10.md"
echo "Lab guide:  $ROOT/docs/LAB_INTEGRATION.md"
echo "Quiz API:   http://localhost:8088/api/v1/quiz/submit"
echo ""
echo "Also start: virtulab-lab (npm run dev :3002) + virtulab-frontend (:3000)"
echo "Starting quiz-service (8088)..."

java -jar "$JAR" &
echo "PID quiz=$!"
wait
