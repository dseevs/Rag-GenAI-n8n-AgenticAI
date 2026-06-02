#!/usr/bin/env bash
# VirtuLab Phase 4 — enable pgvector + rag/agents schemas (run once)
set -euo pipefail
ROOT="$(dirname "$0")/.."
PGHOST="${DB_HOST:-localhost}"
PGPORT="${DB_PORT:-5434}"
PGUSER="${DB_USER:-virtulab}"
PGDATABASE="${DB_NAME:-virtulab}"

echo "Applying Phase 4 SQL to $PGHOST:$PGPORT/$PGDATABASE ..."
if ! PGPASSWORD="${DB_PASSWORD:-virtulab}" psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -f "$ROOT/deploy/phase4/init-pgvector.sql"; then
  echo "ERROR: init failed. Use Postgres image pgvector/pgvector:pg16 (see docs/PHASE4.md)."
  exit 1
fi
echo "Phase 4 database init OK."
