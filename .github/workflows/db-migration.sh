#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./db-migration.sh

readonly DB_MIGRATION="log-db-migration"
readonly QUERY_DB_MIGRATION="SELECT version,installed_on FROM flyway_schema_history ORDER BY installed_on DESC LIMIT 1;"

log() {
  echo -e "\033[1m>>\033[0m ${1}"
}

log_db_migration() {
  # ...
  [[ $(docker ps -f "name=${DB_MIGRATION}" --format '{{.Names}}') == "${DB_MIGRATION}" ]] || \
    docker run -d --name "${DB_MIGRATION}"  \
        -v marquez_db-backup:/var/lib/postgresql/data \
        postgres:12.1
  # ...
  log "latest migration applied to db:"
  docker exec log-db-migration \
    psql -U marquez -c "${QUERY_DB_MIGRATION}"
}

cleanup() {
  log "running db migration cleanup..."
  docker rm -f "${DB_MIGRATION}" > /dev/null
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/"

# (1) ...
log "start db with latest migrations:"
./docker/up.sh \
  --args "--exit-code-from seed_marquez" \
  --tag "latest" \
  --no-web \
  --seed && log_db_migration

# (2) ...
log "start db using backup:"
./docker/up.sh \
  --args "--exit-code-from seed_marquez" \
  --no-web \
  --no-volumes \
  --build \
  --seed > /dev/null && log_db_migration

cleanup

log "DONE!"
