#!/usr/bin/env bash
set -euo pipefail
export KEYCLOAK_ISSUER="${KEYCLOAK_ISSUER:-http://localhost:9080/realms/virtulab}"
export SESSION_SERVICE_URL="${SESSION_SERVICE_URL:-http://localhost:8082}"
export EVENTS_SERVICE_URL="${EVENTS_SERVICE_URL:-http://localhost:8083}"
cd "$(dirname "$0")/../services/graphql-gateway-service"
exec java -jar target/graphql-gateway-service-0.1.0-SNAPSHOT.jar
