#!/usr/bin/env bash
# VirtuLab Phase 6 — RabbitMQ + WebSocket + notifications
set -euo pipefail
ROOT="$(dirname "$0")/.."
export REDIS_PORT="${REDIS_PORT:-6380}"
export RABBITMQ_PORT="${RABBITMQ_PORT:-5673}"
export RABBITMQ_USER="${RABBITMQ_USER:-virtulab}"
export RABBITMQ_PASSWORD="${RABBITMQ_PASSWORD:-virtulab}"
export KEYCLOAK_ISSUER="${KEYCLOAK_ISSUER:-http://localhost:9080/realms/virtulab}"

cd "$ROOT/deploy"
echo "Starting RabbitMQ (5673 / management 15673)..."
docker compose -f docker-compose.yml -f docker-compose.phase6.yml up -d virtulab-rabbitmq

JAR_WS="$ROOT/services/websocket-gateway-service/target/websocket-gateway-service-0.1.0-SNAPSHOT.jar"
JAR_NOTIF="$ROOT/services/notification-service/target/notification-service-0.1.0-SNAPSHOT.jar"

for j in "$JAR_WS" "$JAR_NOTIF"; do
  if [[ ! -f "$j" ]]; then
    echo "Missing $j — run ./scripts/build.sh first"
    exit 1
  fi
done

echo "=== VirtuLab Phase 6 ==="
echo "Docs: $ROOT/docs/PHASE6.md"
echo "WebSocket: ws://localhost:8090/api/v1/ws/live?token=<access_token>"
echo "Requires: ./scripts/run-all.sh (lab-events publishes to Redis + RabbitMQ)"

java -jar "$JAR_WS" &
PID_WS=$!
java -jar "$JAR_NOTIF" &
PID_NOTIF=$!

echo "PIDs: websocket=$PID_WS notification=$PID_NOTIF"
wait
