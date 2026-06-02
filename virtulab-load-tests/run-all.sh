#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
"$ROOT/run-smoke.sh"
echo ""
TOKEN="$("$ROOT/scripts/get-token.sh")"
export TOKEN
"$ROOT/run-progress-storm.sh"
