#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# A script used in CI to load test HTTP API server by:
#   (1) Starting HTTP API server
#   (2) Generating random dataset, job, and run metadata
#   (3) Run load test using k6
#
# Usage: $ ./api-load-test.sh

# Build version of Marquez
readonly MARQUEZ_BUILD_VERSION="$(git log --pretty=format:'%h' -n 1)" # SHA1

log() {
  echo -e "\033[1m>>\033[0m ${1}"
}

error() {
  echo -e "\033[0;31merror: ${1}\033[0m"
}

exit_with_cause() {
  log "please view container logs for more details on cause:"
  docker-compose logs
  exit 1
}

# (1) Start HTTP API server
log "start HTTP API server (marquez=${MARQUEZ_BUILD_VERSION}):"
if ! ./docker/up.sh \
  --no-web \
  --build; then
  error "failed to start db using backup!"
  exit_with_cause
fi

# (2) Use metadata command to generate random dataset, job, and run metadata
java -jar api/build/libs/marquez-api-*.jar metadata --runs 10 --bytes-per-event 16384

# (3) Run load test
k6 run --vus 25 --duration 30s api/load-testing/http.js \
  --out json=./k6/full.json --summary-export=./k6/summary.json

echo "DONE!"
