#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./db-migration.sh

readonly DB_MIGRATION_BACKUP="db-migration-backup"
readonly DB_MIGRATION_QUERY=$(cat <<-END
  SELECT version,installed_on,checksum
    FROM flyway_schema_history
   WHERE version IS NOT NULL
   ORDER BY installed_on DESC LIMIT 1;
END
)

log() {
  echo -e "\033[1m>>\033[0m ${1}"
}

query_db_migration() {
  # Start db using backup
  [[ $(docker ps -f "name=${DB_MIGRATION_BACKUP}" --format '{{.Names}}') == "${DB_MIGRATION_BACKUP}" ]] || \
    docker run -d --name "${DB_MIGRATION_BACKUP}" \
        -v marquez_db-backup:/var/lib/postgresql/data \
        postgres:12.1
  # Query applied db migrations
  log "latest migration applied to db:"
  docker exec "${DB_MIGRATION_BACKUP}" \
    psql -U marquez -c "${DB_MIGRATION_QUERY}"
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/"

# (1) Apply db migrations on latest Marquez release
log "start db with latest migrations:"
if ! ./docker/up.sh \
  --args "--exit-code-from seed_marquez" \
  --tag "latest" \
  --no-web \
  --seed > /dev/null; then
  exit 1
fi

query_db_migration

# (2) Apply db migrations on latest Marquez build using backup
log "start db using backup:"
if ! ./docker/up.sh \
  --args "--exit-code-from seed_marquez" \
  --no-web \
  --no-volumes \
  --build \
  --seed > /dev/null; then
  exit 1
fi

query_db_migration

log "DONE!"
