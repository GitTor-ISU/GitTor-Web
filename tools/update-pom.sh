#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$SCRIPT_DIR/../api"

$API_DIR/mvnw versions:use-latest-releases --file $API_DIR/pom.xml -DgenerateBackupPoms=false
