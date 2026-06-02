#!/usr/bin/env bash
# VirtuLab Phase 4 — Ollama + AI services (requires: build, init-phase4-db, Phase 1-2 running optional)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export DB_PORT="${DB_PORT:-5434}"
export REDIS_PORT="${REDIS_PORT:-6380}"
export KAFKA_BOOTSTRAP="${KAFKA_BOOTSTRAP:-localhost:9095}"
export KEYCLOAK_ISSUER="${KEYCLOAK_ISSUER:-http://localhost:9080/realms/virtulab}"
export CORPUS_PATH="${CORPUS_PATH:-$ROOT/rag-corpus}"
export OLLAMA_BASE_URL="${OLLAMA_BASE_URL:-http://localhost:11434}"
export AI_TUTOR_URL="${AI_TUTOR_URL:-http://localhost:8084}"

cd "$ROOT/deploy"
if curl -sf http://localhost:11434/ >/dev/null 2>&1; then
  echo "Using existing Ollama on http://localhost:11434 (skip Docker container)."
else
  echo "Starting virtulab-ollama container..."
  if ! docker compose -f docker-compose.yml -f docker-compose.phase4.yml up -d virtulab-ollama 2>&1; then
    echo "WARN: Could not start virtulab-ollama (port 11434 busy?). Use host Ollama or set OLLAMA_BASE_URL."
  fi
fi

JAR_RAG="$ROOT/services/rag-ingest-service/target/rag-ingest-service-0.1.0-SNAPSHOT.jar"
JAR_AI="$ROOT/services/ai-tutor-service/target/ai-tutor-service-0.1.0-SNAPSHOT.jar"
JAR_AGENT="$ROOT/services/agent-orchestrator-service/target/agent-orchestrator-service-0.1.0-SNAPSHOT.jar"

for j in "$JAR_RAG" "$JAR_AI" "$JAR_AGENT"; do
  if [[ ! -f "$j" ]]; then
    echo "Missing $j — run ./scripts/build.sh first"
    exit 1
  fi
done

echo "Starting Phase 4: rag-ingest (8086), ai-tutor (8084), agent-orchestrator (8085)..."
echo "Docs: $ROOT/docs/PHASE4.md"

java -jar "$JAR_RAG" &
PID_RAG=$!
java -jar "$JAR_AI" &
PID_AI=$!
java -jar "$JAR_AGENT" &
PID_AGENT=$!

echo "PIDs: rag=$PID_RAG ai=$PID_AI agent=$PID_AGENT"
echo "Optional: ollama pull nomic-embed-text && ollama pull llama3.2"
wait
