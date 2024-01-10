#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# A script used in CI to test database migrations by:
#   (1) Applying db migrations on latest Marquez release
#   (2) Taking a backup of db from Step 1
#   (3) Applying db migrations on latest Marquez build using backup
#
# Usage: $ ./db-migration.sh

# Version of PostgreSQL
readonly POSTGRES_VERSION="14"
# Version of Marquez
readonly MARQUEZ_VERSION=0.43.1
# Build version of Marquez
readonly MARQUEZ_BUILD_VERSION="$(git log --pretty=format:'%h' -n 1)" # SHA1

readonly DB_MIGRATION_VOLUME="marquez_db-backup"
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

error() {
  echo -e "\033[0;31merror: ${1}\033[0m"
}

exit_with_cause() {
  log "please view container logs for more details on cause:"
  docker-compose logs
  exit 1
}

query_db_migration() {
  # Start db using backup
  [[ $(docker ps -f "name=${DB_MIGRATION_BACKUP}" --format '{{.Names}}') == "${DB_MIGRATION_BACKUP}" ]] || \
    docker run -d --name "${DB_MIGRATION_BACKUP}" \
      -v "${DB_MIGRATION_VOLUME}:/var/lib/postgresql/data" \
      "postgres:${POSTGRES_VERSION}"
  # Query applied db migrations
  log "latest migration applied to db:"
  docker exec "${DB_MIGRATION_BACKUP}" \
    psql -U marquez -c "${DB_MIGRATION_QUERY}"
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/"

# (1) Apply db migrations on latest Marquez release
log "start db with latest migrations (marquez=${MARQUEZ_VERSION}):"
if ! ./docker/up.sh \
  --args "--exit-code-from seed_marquez" \
  --tag "${MARQUEZ_VERSION}" \
  --no-web \
  --seed > /dev/null; then
  error "failed to start db using backup!"
  exit_with_cause
fi

# Query, then display schema migration applied
query_db_migration

# (2) Apply db migrations on latest Marquez build using backup
log "start db using backup (marquez=${MARQUEZ_BUILD_VERSION}):"
if ! ./docker/up.sh \
  --args "--exit-code-from seed_marquez" \
  --no-web \
  --no-volumes \
  --build \
  --seed > /dev/null; then
  error "failed to start db using backup!"
  exit_with_cause
fi

# Query, then display additional schema migration applied on backup (if any)
query_db_migration

log "DONE!"
