#!/usr/bin/env bash
# Fetch Keycloak access token for JMeter / load tests
set -euo pipefail
KC="${KEYCLOAK_URL:-http://localhost:9080/realms/virtulab/protocol/openid-connect/token}"
USER="${VIRTULAB_USER:-student1}"
PASS="${VIRTULAB_PASS:-password}"

curl -s -X POST "$KC" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'client_id=virtulab-postman' \
  --data-urlencode 'client_secret=virtulab-postman-secret' \
  --data-urlencode "username=$USER" \
  --data-urlencode "password=$PASS" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])"
