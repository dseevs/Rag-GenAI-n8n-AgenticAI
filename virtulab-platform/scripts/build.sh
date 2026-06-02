#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if command -v mvn >/dev/null 2>&1; then
  mvn -q clean package -DskipTests
else
  echo "Maven not found — building with Docker..."
  docker run --rm -v "$(pwd)":/w -w /w maven:3.9.9-eclipse-temurin-17 mvn -q clean package -DskipTests
fi
echo "Build OK."
