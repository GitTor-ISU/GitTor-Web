#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/../"

export JWT_TOKEN_SECRET=$(head -c 64 /dev/urandom | base64 -w 0)
export API_ADMIN_USERNAME=admin
export API_ADMIN_PASSWORD=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c16)

# Building containers (mostly generating Open API code for the frontend)
docker compose -f "$ROOT_DIR/compose.yml" -f "$ROOT_DIR/compose.dev.yml" down
docker compose -f "$ROOT_DIR/compose.yml" -f "$ROOT_DIR/compose.dev.yml" up --build -d api
echo "Waiting for API to be healthy..."
until [ "$(docker inspect -f '{{.State.Health.Status}}' "$(docker compose ps -q api)")" = "healthy" ]; do
    sleep 1
done
docker compose -f "$ROOT_DIR/compose.yml" build --build-arg CACHEBUST=$(date +%s) ui

# Remove extra containers and extra exposed ports
docker compose -f "$ROOT_DIR/compose.yml" down

# Start up production application
docker compose -f "$ROOT_DIR/compose.yml" up -d

echo "API_ADMIN_USERNAME: $API_ADMIN_USERNAME"
echo "API_ADMIN_PASSWORD: $API_ADMIN_PASSWORD"
