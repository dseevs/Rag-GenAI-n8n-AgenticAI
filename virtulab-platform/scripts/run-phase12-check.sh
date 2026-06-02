#!/usr/bin/env bash
set -euo pipefail
ROOT="$(dirname "$0")/.."

echo "=== VirtuLab Phase 12 health check ==="

tcp_check() {
  local port=$1 name=$2
  if timeout 1 bash -c "echo >/dev/tcp/localhost/$port" 2>/dev/null; then
    echo "OK  $name (port $port)"
  else
    echo "FAIL $name (port $port)"
    return 1
  fi
}

http_check() {
  local url=$1 name=$2
  if curl -sf "$url" >/dev/null 2>&1; then
    echo "OK  $name"
  else
    echo "FAIL $name ($url)"
    return 1
  fi
}

fail=0
tcp_check 5434 "Postgres" || fail=$((fail + 1))
tcp_check 6380 "Redis" || fail=$((fail + 1))
http_check "http://localhost:8081/actuator/health" "auth-service" || fail=$((fail + 1))
http_check "http://localhost:8097/actuator/health" "graphql-gateway" || fail=$((fail + 1))
http_check "http://localhost:8800/status" "Kong" || fail=$((fail + 1))

if command -v kubectl >/dev/null 2>&1 && kubectl get ns virtulab >/dev/null 2>&1; then
  echo ""
  kubectl get pods -n virtulab 2>/dev/null || true
fi

echo ""
if [[ "$fail" -eq 0 ]]; then
  echo "Phase 12 check passed."
else
  echo "$fail check(s) failed — start: cd deploy && docker compose up -d && ./scripts/run-all.sh"
  exit 1
fi
