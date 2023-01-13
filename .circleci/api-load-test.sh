#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# A script used in CI to load test HTTP API server by:
#   (1) Starting HTTP API server
#   (2) Generating random dataset, job, and run metadata
#   (3) Running load test using k6
#
# Usage: $ ./api-load-test.sh

set -e

# Version of Marquez
readonly MARQUEZ_VERSION="0.30.0-SNAPSHOT"

# Build version of Marquez
readonly METADATA_FILE="api/load-testing/metadata.json"

log() {
  echo -e "\033[1m>>\033[0m ${1}"
}

error() {
  echo -e "\033[0;31merror: ${1}\033[0m"
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

# (1) Start db
log "start db:"
docker-compose -f docker-compose.db.yml up --detach

# (2) Build HTTP API server
log "build http server:"
./gradlew --no-daemon :api:build -x test

# (3) Start HTTP API server
java -jar "api/build/libs/marquez-api-${MARQUEZ_VERSION}.jar" server marquez.dev.yml &

# (4) Use metadata command to generate random dataset, job, and run metadata
log "generate load test metadata (${METADATA_FILE}):"
java -jar "api/build/libs/marquez-api-${MARQUEZ_VERSION}.jar" metadata --runs 10 --bytes-per-event 16384 --output "${METADATA_FILE}"

# (5) Run load test
log "start load test:"
mkdir -p k6/results && \
  k6 run --vus 25 --duration 30s api/load-testing/http.js \
    --out json=k6/results/full.json --summary-export=k6/results/summary.json

echo "DONE!"
