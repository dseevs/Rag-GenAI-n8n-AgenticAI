#!/usr/bin/env bash
# VirtuLab Phase 5 — analytics ingest (8089) + query (8091)
set -euo pipefail
ROOT="$(dirname "$0")/.."
export DB_PORT="${DB_PORT:-5434}"
export REDIS_PORT="${REDIS_PORT:-6380}"
export KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-localhost:9095}"
export KEYCLOAK_ISSUER="${KEYCLOAK_ISSUER:-http://localhost:9080/realms/virtulab}"

JAR_INGEST="$ROOT/services/analytics-ingest-service/target/analytics-ingest-service-0.1.0-SNAPSHOT.jar"
JAR_QUERY="$ROOT/services/analytics-query-service/target/analytics-query-service-0.1.0-SNAPSHOT.jar"

for j in "$JAR_INGEST" "$JAR_QUERY"; do
  if [[ ! -f "$j" ]]; then
    echo "Missing $j — run ./scripts/build.sh first"
    exit 1
  fi
done

echo "=== VirtuLab Phase 5 ==="
echo "Docs: $ROOT/docs/PHASE5.md"
echo "Starting analytics-ingest (8089), analytics-query (8091)..."

java -jar "$JAR_INGEST" &
PID_INGEST=$!
java -jar "$JAR_QUERY" &
PID_QUERY=$!

echo "PIDs: ingest=$PID_INGEST query=$PID_QUERY"
echo "Requires: docker compose up -d, ./scripts/run-all.sh (for Kafka producers)"
wait
