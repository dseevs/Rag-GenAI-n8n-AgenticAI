#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPORT_DIR="${REPORT_DIR:-$ROOT/reports/analytics-$(date +%Y%m%d-%H%M%S)}"
mkdir -p "$REPORT_DIR"
TOKEN="$("$ROOT/scripts/get-token.sh" 2>/dev/null || echo "")"

if [[ -z "$TOKEN" ]]; then
  echo "Need Keycloak token — start docker compose first"
  exit 1
fi

if command -v jmeter >/dev/null 2>&1; then
  jmeter -n -t "$ROOT/jmeter/analytics-heavy.jmx" \
    -l "$REPORT_DIR/results.jtl" -e -o "$REPORT_DIR/html" \
    -Jtoken="$TOKEN" -Jthreads="${THREADS:-50}" -Jduration="${DURATION:-600}"
  echo "Report: $REPORT_DIR/html/index.html"
else
  echo "JMeter not installed — curl 20 org-funnel requests..."
  ok=0
  for i in $(seq 1 20); do
    curl -sf -H "Authorization: Bearer $TOKEN" \
      "http://localhost:8091/api/v1/analytics/org-funnel?orgId=org-dev" >/dev/null && ok=$((ok + 1))
  done
  echo "ok=$ok/20" | tee "$REPORT_DIR/summary.txt"
fi
