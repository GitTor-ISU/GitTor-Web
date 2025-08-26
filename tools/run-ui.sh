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

if curl -fsS http://localhost:8080 | grep -q true; then
    echo "Backend is already running."
else
    $API_DIR/mvnw --file $API_DIR/pom.xml \
        resources:resources \
        compiler:compile \
        jar:jar \
        spring-boot:repackage

    ( cd "$API_DIR" && java -jar target/api-1.jar ) &
    API_PID=$!
    sleep 1
fi

until curl -fsS http://localhost:8080 | grep -q true; do sleep 1; done

curl -sf http://localhost:8080/v3/api-docs -o /tmp/openapi.json
rm -rf $UI_DIR/src/app/api
npx @openapitools/openapi-generator-cli generate \
    -i /tmp/openapi.json \
    -g typescript-angular \
    -o $UI_DIR/src/app/api \
    --openapitools $UI_DIR/openapitools.json

( cd "$UI_DIR" && npm start )
