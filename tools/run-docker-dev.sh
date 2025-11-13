#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/../"

export JWT_TOKEN_SECRET=tJ4AQBHzsJQudcQ5NT11oi77967OfacU3mEMyYfXl09adbVTKA0cgjsleAdvJkO/wGMSO3KfBn4xO3z+hpWYGw==
export API_ADMIN_USERNAME=admin
export API_ADMIN_EMAIL=admin@gittor
export API_ADMIN_PASSWORD=password

docker compose -f "$ROOT_DIR/compose.yml" -f "$ROOT_DIR/compose.dev.yml" down
docker compose -f "$ROOT_DIR/compose.yml" -f "$ROOT_DIR/compose.dev.yml" up --build -d

echo "API_ADMIN_USERNAME: $API_ADMIN_USERNAME"
echo "API_ADMIN_EMAIL: $API_ADMIN_EMAIL"
echo "API_ADMIN_PASSWORD: $API_ADMIN_PASSWORD"
