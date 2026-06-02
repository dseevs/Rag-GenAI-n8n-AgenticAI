#!/usr/bin/env bash
# VirtuLab smoke load test — JMeter if installed, else parallel curl fallback
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPORT_DIR="${REPORT_DIR:-$ROOT/reports/smoke-$(date +%Y%m%d-%H%M%S)}"
mkdir -p "$REPORT_DIR"

echo "=== VirtuLab smoke load test ==="
echo "Report: $REPORT_DIR"

if command -v jmeter >/dev/null 2>&1; then
  jmeter -n \
    -t "$ROOT/jmeter/smoke.jmx" \
    -l "$REPORT_DIR/results.jtl" \
    -e -o "$REPORT_DIR/html" \
    -Jthreads=10 \
    -Jduration=60
  echo "JMeter HTML report: $REPORT_DIR/html/index.html"
  exit 0
fi

echo "JMeter not found — running curl smoke (10 parallel × 5 endpoints)..."

endpoints=(
  "http://localhost:8081/actuator/health"
  "http://localhost:8082/actuator/health"
  "http://localhost:8083/actuator/health"
  "http://localhost:8097/actuator/health"
  "http://localhost:8800/status"
)

ok=0
fail=0
for i in $(seq 1 10); do
  for url in "${endpoints[@]}"; do
    if curl -sf "$url" >/dev/null; then ok=$((ok + 1)); else fail=$((fail + 1)); fi
  done
done

echo "Results: ok=$ok fail=$fail" | tee "$REPORT_DIR/summary.txt"
[[ "$fail" -eq 0 ]]
