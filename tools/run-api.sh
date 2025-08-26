#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$SCRIPT_DIR/../api"

$API_DIR/mvnw --file $API_DIR/pom.xml \
    resources:resources \
    compiler:compile \
    jar:jar \
    spring-boot:repackage

( cd "$API_DIR" && java -jar target/api-1.jar )
