#!/usr/bin/env bash
# Start VirtuLab Phase 1 + Phase 2 backend (requires: ./scripts/build.sh, docker compose up -d)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export DB_PORT="${DB_PORT:-5434}"
export REDIS_PORT="${REDIS_PORT:-6380}"
export KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-localhost:9095}"
export JWT_SECRET="${JWT_SECRET:-virtulab-dev-secret-change-me-32chars-minimum!!}"
export KEYCLOAK_ISSUER="${KEYCLOAK_ISSUER:-http://localhost:9080/realms/virtulab}"
export SESSION_SERVICE_URL="${SESSION_SERVICE_URL:-http://localhost:8082}"
export EVENTS_SERVICE_URL="${EVENTS_SERVICE_URL:-http://localhost:8083}"

JAR_AUTH="$ROOT/services/auth-service/target/auth-service-0.1.0-SNAPSHOT.jar"
JAR_SESSION="$ROOT/services/session-service/target/session-service-0.1.0-SNAPSHOT.jar"
JAR_EVENTS="$ROOT/services/lab-events-service/target/lab-events-service-0.1.0-SNAPSHOT.jar"
JAR_GRAPHQL="$ROOT/services/graphql-gateway-service/target/graphql-gateway-service-0.1.0-SNAPSHOT.jar"

for j in "$JAR_AUTH" "$JAR_SESSION" "$JAR_EVENTS" "$JAR_GRAPHQL"; do
  if [[ ! -f "$j" ]]; then
    echo "Missing $j — run ./scripts/build.sh first"
    exit 1
  fi
done

echo "Starting auth (8081), session (8082), events (8083), graphql (8097)..."
echo "Keycloak issuer: $KEYCLOAK_ISSUER"
echo "Stop with: pkill -f 'auth-service-0.1.0-SNAPSHOT.jar' (or kill PIDs below)"

java -jar "$JAR_AUTH" &
PID_AUTH=$!
java -jar "$JAR_SESSION" &
PID_SESSION=$!
java -jar "$JAR_EVENTS" &
PID_EVENTS=$!
java -jar "$JAR_GRAPHQL" &
PID_GRAPHQL=$!

echo "PIDs: auth=$PID_AUTH session=$PID_SESSION events=$PID_EVENTS graphql=$PID_GRAPHQL"
wait
