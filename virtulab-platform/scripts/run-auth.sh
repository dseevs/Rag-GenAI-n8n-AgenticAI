#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../services/auth-service"
exec java -jar target/auth-service-0.1.0-SNAPSHOT.jar
