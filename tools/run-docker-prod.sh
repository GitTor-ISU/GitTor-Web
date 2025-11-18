#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/../"

export JWT_TOKEN_SECRET=$(head -c 64 /dev/urandom | base64 -w 0)
export API_ADMIN_USERNAME=admin
export API_ADMIN_EMAIL=admin@gittor
export API_ADMIN_PASSWORD=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c16)

docker compose -f "$ROOT_DIR/compose.yml" -f "$ROOT_DIR/compose.dev.yml" down
docker compose -f "$ROOT_DIR/compose.yml" up --build -d

echo "API_ADMIN_USERNAME: $API_ADMIN_USERNAME"
echo "API_ADMIN_EMAIL: $API_ADMIN_EMAIL"
echo "API_ADMIN_PASSWORD: $API_ADMIN_PASSWORD"
