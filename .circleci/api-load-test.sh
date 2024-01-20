#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# A script used in CI to load test HTTP API server by:
#   (1) Starting HTTP API server
#   (2) Generating random dataset, job, and run metadata
#   (3) Running load test using k6
#   (4) Writing load test results to 'k6/results' for analysis
#
# Usage: $ ./api-load-test.sh

set -e

# Build version of Marquez
readonly MARQUEZ_VERSION=0.45.0-SNAPSHOT
# Fully qualified path to marquez.jar
readonly MARQUEZ_JAR="api/build/libs/marquez-api-${MARQUEZ_VERSION}.jar"

readonly MARQUEZ_HOST="localhost"
readonly MARQUEZ_ADMIN_PORT=8081
readonly MARQUEZ_URL="http://${MARQUEZ_HOST}:${MARQUEZ_ADMIN_PORT}"
readonly MARQUEZ_DB="marquez-db"

readonly METADATA_FILE="api/load-testing/metadata.json"
readonly METADATA_STATS_QUERY=$(cat <<-END
  SELECT run_uuid,COUNT(*)
    FROM lineage_events
   GROUP BY run_uuid;
END
)

# marquez.yml
cat > marquez.yml <<EOF
server:
  applicationConnectors:
  - type: http
    port: 8080
    httpCompliance: RFC7230_LEGACY
  adminConnectors:
  - type: http
    port: 8081

db:
  driverClass: org.postgresql.Driver
  url: jdbc:postgresql://localhost:5432/marquez
  user: marquez
  password: marquez

migrateOnStartup: true
EOF

log() {
  echo -e "\033[1m>>\033[0m ${1}"
}

cpu_and_mem_info() {
  log "CPU info:"
  cat /proc/cpuinfo
  log "MEM info:"
  cat /proc/meminfo
}

metadata_stats() {
  # Query db for metadata stats
  log "load test metadata stats:"
  docker exec "${MARQUEZ_DB}" \
    psql -U marquez -c "${METADATA_STATS_QUERY}"
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

# (1) Start db
log "start db:"
docker-compose -f docker-compose.db.yml up --detach

# (2) Build HTTP API server
log "build http API server..."
./gradlew --no-daemon :api:build -x test > /dev/null 2>&1

# (3) Start HTTP API server
log "start http API server..."
mkdir marquez && \
  java -jar "${MARQUEZ_JAR}" server marquez.yml > marquez/http.log 2>&1 &

# (4) Wait for HTTP API server
log "waiting for http API server (${MARQUEZ_URL})..."
until curl --output /dev/null --silent --head --fail "${MARQUEZ_URL}/ping"; do
    sleep 5
done
# When available, print status
log "http API server is ready!"

# (5) Use metadata command to generate random dataset, job, and run metadata
log "generate load test metadata (${METADATA_FILE}):"
java -jar "${MARQUEZ_JAR}" metadata --runs 10 --bytes-per-event 16384 --output "${METADATA_FILE}"

# Display CPU/MEM
cpu_and_mem_info

# (6) Run load test
log "start load test:"
mkdir -p k6/results && \
  k6 run --vus 25 --duration 30s api/load-testing/http.js \
    --out json=k6/results/full.json --summary-export=k6/results/summary.json

# Display metadata stats
metadata_stats

echo "DONE!"
