#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../services/lab-events-service"
export DB_PORT=5434 REDIS_PORT=6380 KAFKA_BOOTSTRAP=localhost:9095
exec java -jar target/lab-events-service-0.1.0-SNAPSHOT.jar
