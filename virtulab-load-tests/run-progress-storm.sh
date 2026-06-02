#!/usr/bin/env bash
# Progress storm — GraphQL recordProgress via Kong (requires stack running)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPORT_DIR="${REPORT_DIR:-$ROOT/reports/progress-storm-$(date +%Y%m%d-%H%M%S)}"
mkdir -p "$REPORT_DIR"

TOKEN="$("$ROOT/scripts/get-token.sh")"
ATTEMPT_ID="${ATTEMPT_ID:-load-test-$(uuidgen 2>/dev/null || echo test-attempt)}"

echo "=== VirtuLab progress storm ==="
echo "Report: $REPORT_DIR"
echo "Attempt: $ATTEMPT_ID"

if command -v jmeter >/dev/null 2>&1; then
  jmeter -n \
    -t "$ROOT/jmeter/progress-storm.jmx" \
    -l "$REPORT_DIR/results.jtl" \
    -e -o "$REPORT_DIR/html" \
    -Jthreads="${THREADS:-50}" \
    -Jduration="${DURATION:-300}" \
    -Jtoken="$TOKEN" \
    -JattemptId="$ATTEMPT_ID" \
    -JgraphqlUrl="${GRAPHQL_URL:-http://localhost:8800/graphql}"
  echo "JMeter HTML report: $REPORT_DIR/html/index.html"
  exit 0
fi

echo "JMeter not found — running lightweight bash burst (${BURST:-30} requests)..."

GRAPHQL="${GRAPHQL_URL:-http://localhost:8097/graphql}"
BURST="${BURST:-30}"
ok=0
fail=0

for i in $(seq 1 "$BURST"); do
  code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$GRAPHQL" \
    -H "Authorization: Bearer $TOKEN" \
    -H 'Content-Type: application/json' \
    -d "{\"query\":\"mutation(\$in: ProgressInput!) { recordProgress(input: \$in) { eventId duplicate } }\",\"variables\":{\"in\":{\"experimentId\":\"v1-chemistry\",\"attemptId\":\"$ATTEMPT_ID\",\"stepId\":\"load-$i\",\"progress\":{\"tab\":\"simulation\",\"percentage\":$((i % 100))}}}}")
  if [[ "$code" == "200" ]]; then ok=$((ok + 1)); else fail=$((fail + 1)); fi
done

echo "Results: ok=$ok fail=$fail" | tee "$REPORT_DIR/summary.txt"
[[ "$fail" -lt $((BURST / 10 + 1)) ]]
