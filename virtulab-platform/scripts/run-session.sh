#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../services/session-service"
export DB_PORT=5434 REDIS_PORT=6380
exec java -jar target/session-service-0.1.0-SNAPSHOT.jar
