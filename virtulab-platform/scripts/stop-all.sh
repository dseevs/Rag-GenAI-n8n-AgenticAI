#!/usr/bin/env bash
# Stop all VirtuLab Java services + Docker stack (free ports 8081–8097, 5434, 6380, 9095, 8800, 9080, 9091, 3001)
set -euo pipefail
ROOT="$(dirname "$0")/.."
DEPLOY="$ROOT/deploy"

echo "=== Stopping VirtuLab Java services ==="
# Match our Spring Boot fat JARs
pkill -f 'auth-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'session-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'lab-events-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'graphql-gateway-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'rag-ingest-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'ai-tutor-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'agent-orchestrator-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'analytics-ingest-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'analytics-query-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'websocket-gateway-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'notification-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'audit-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'ml-scoring-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true
pkill -f 'quiz-service-0.1.0-SNAPSHOT.jar' 2>/dev/null || true

sleep 1

# Force-free app ports if something still holds them
for port in 8081 8082 8083 8084 8085 8086 8088 8089 8090 8091 8092 8095 8096 8097; do
  if command -v fuser >/dev/null 2>&1; then
    fuser -k "${port}/tcp" 2>/dev/null || true
  fi
done

echo "=== Stopping Docker (VirtuLab deploy) ==="
cd "$DEPLOY"
if [[ -f docker-compose.phase7.yml ]]; then
  docker compose -f docker-compose.yml -f docker-compose.phase3.yml -f docker-compose.phase4.yml -f docker-compose.phase6.yml -f docker-compose.phase7.yml down 2>/dev/null || true
fi
if [[ -f docker-compose.phase6.yml ]]; then
  docker compose -f docker-compose.yml -f docker-compose.phase3.yml -f docker-compose.phase4.yml -f docker-compose.phase6.yml down 2>/dev/null || true
fi
if [[ -f docker-compose.phase3.yml ]]; then
  docker compose -f docker-compose.yml -f docker-compose.phase3.yml down 2>/dev/null || true
fi
docker compose down 2>/dev/null || true

echo ""
echo "=== Port status ==="
for port in 8081 8082 8083 8084 8085 8086 8088 8089 8090 8091 8092 8095 8096 8097 9080 8800 8801 9091 3001 5434 6380 9095 5673 15673 5680 3002; do
  if ss -tln 2>/dev/null | grep -q ":${port} "; then
    echo "  $port  IN USE"
  else
    echo "  $port  free"
  fi
done

echo ""
echo "Note: Ollama on 11434 is usually your HOST install — not stopped by this script."
echo "      Next.js frontend on 3000 is separate — stop with: pkill -f 'next dev' or Ctrl+C in that terminal."
echo "Done. Start fresh with: cd deploy && docker compose up -d && ./scripts/run-all.sh"
