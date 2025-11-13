#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$SCRIPT_DIR/../api"
UI_DIR="$SCRIPT_DIR/../ui"

rm -rf $UI_DIR/src/app/generated/openapi
$API_DIR/mvnw --file $API_DIR/pom.xml \
    resources:resources \
    compiler:compile \
    jar:jar \
    spring-boot:repackage \
    spring-boot:start \
    springdoc-openapi:generate \
    spring-boot:stop
npx @redocly/cli@latest build-docs $API_DIR/target/openapi.json \
    -o $API_DIR/target/site/openapi.html
npx @openapitools/openapi-generator-cli generate \
    -i $API_DIR/target/openapi.json \
    -g typescript-angular \
    -o $UI_DIR/src/app/generated/openapi \
    --openapitools $UI_DIR/openapitools.json \
    -c $UI_DIR/openapiconfig.json
