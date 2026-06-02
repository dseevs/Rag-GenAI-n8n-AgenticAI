#!/usr/bin/env bash
# VirtuLab — Boss demo: RAG + GenAI + Agentic AI (Phase 4 proof)
# Prereq: docker compose up, ./scripts/run-all.sh, ./scripts/run-phase4.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KC="http://localhost:9080/realms/virtulab/protocol/openid-connect/token"

say() { echo ""; echo "========================================"; echo "  $*"; echo "========================================"; }

get_token() {
  local user=$1
  curl -s -X POST "$KC" \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'grant_type=password' \
    --data-urlencode 'client_id=virtulab-postman' \
    --data-urlencode 'client_secret=virtulab-postman-secret' \
    --data-urlencode "username=$user" \
    --data-urlencode 'password=password' \
    | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])"
}

pretty() {
  if command -v jq >/dev/null 2>&1; then jq .; else python3 -m json.tool 2>/dev/null || cat; fi
}

say "Health check (AI services)"
for url in \
  "http://localhost:8084/actuator/health" \
  "http://localhost:8085/actuator/health" \
  "http://localhost:8086/actuator/health"; do
  curl -sf "$url" | head -c 80 && echo " ... $url" || { echo "FAIL $url — run ./scripts/run-phase4.sh"; exit 1; }
done

TOKEN_D=$(get_token dev1)
TOKEN_S=$(get_token student1)
echo "Tokens OK (dev1 + student1)"

say "1) RAG — Index lab knowledge into vector DB (pgvector)"
REINDEX=$(curl -s -X POST http://localhost:8086/api/v1/rag/reindex \
  -H "Authorization: Bearer $TOKEN_D" \
  -H 'Content-Type: application/json' \
  -d '{"experimentId":"v1-chemistry"}')
echo "$REINDEX" | pretty
JOB_ID=$(echo "$REINDEX" | python3 -c "import sys,json; print(json.load(sys.stdin).get('jobId',''))" 2>/dev/null || true)
if [[ -n "$JOB_ID" ]]; then
  echo "Waiting for reindex job $JOB_ID ..."
  for i in $(seq 1 30); do
    STATUS=$(curl -s "http://localhost:8086/api/v1/rag/jobs/$JOB_ID" -H "Authorization: Bearer $TOKEN_D")
    echo "$STATUS" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('status'), 'chunks=', d.get('chunksIndexed',0))" 2>/dev/null || true
    if echo "$STATUS" | grep -q COMPLETED; then break; fi
    sleep 2
  done
fi

curl -s http://localhost:8086/api/v1/rag/stats -H "Authorization: Bearer $TOKEN_D" | pretty

say "2) GenAI — Food for thought (3 reflection questions)"
curl -s -X POST http://localhost:8084/api/v1/ai/food-for-thought \
  -H "Authorization: Bearer $TOKEN_S" \
  -H 'Content-Type: application/json' \
  -d '{"topic":"acids and bases","experimentId":"v1-chemistry","lang":"en"}' | pretty

say "3) RAG + GenAI — Ask tutor with citations from corpus"
curl -s -X POST http://localhost:8084/api/v1/ai/ask \
  -H "Authorization: Bearer $TOKEN_S" \
  -H 'Content-Type: application/json' \
  -d '{"experimentId":"v1-chemistry","question":"What is the litmus test procedure?","lang":"en"}' | pretty

say "4) Agentic AI — Multi-step agent (postExperimentTutor)"
curl -s -X POST http://localhost:8085/api/v1/agents/run \
  -H "Authorization: Bearer $TOKEN_S" \
  -H 'Content-Type: application/json' \
  -d '{"agentType":"postExperimentTutor","experimentId":"v1-chemistry","lang":"en","userMessage":"I finished the acids lab. Help me reflect without giving me quiz answers."}' | pretty

say "DONE — Show your boss: citations = RAG, questions = GenAI, steps[] = Agentic"
echo "Optional UI: http://localhost:3000 (login student1) + Grafana http://localhost:3001"
