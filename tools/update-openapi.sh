#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$SCRIPT_DIR/../api"
UI_DIR="$SCRIPT_DIR/../ui"

rm -rf $UI_DIR/src/app/api
$API_DIR/mvnw verify --file $API_DIR/pom.xml -DskipTests
npx @redocly/cli@latest build-docs $API_DIR/target/openapi/openapi.json \
    -o $API_DIR/target/site/openapi.html
npx @openapitools/openapi-generator-cli generate \
    -i $API_DIR/target/openapi/openapi.json \
    -g typescript-angular \
    -o $UI_DIR/src/app/api \
    --openapitools $UI_DIR/openapitools.json
