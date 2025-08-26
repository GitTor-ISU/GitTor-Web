#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$SCRIPT_DIR/../api"
UI_DIR="$SCRIPT_DIR/../ui"
API_PID=""

cleanup() {
    if [[ -n "${API_PID}" ]] && kill -0 "$API_PID" 2>/dev/null; then
        echo "Stopping backend (PID $API_PID)..."
        kill "$API_PID"
        wait "$API_PID" 2>/dev/null || true
    fi
}
trap cleanup EXIT

$API_DIR/mvnw --file $API_DIR/pom.xml \
    resources:resources \
    compiler:compile \
    jar:jar \
    spring-boot:repackage

( cd "$API_DIR" && java -jar target/api-1.jar ) &
API_PID=$!

sleep 1
until curl -fsS http://localhost:8080 | grep -q true; do sleep 1; done

cd "$UI_DIR"
npm run e2e
