#!/bin/bash
#
# Usage: $ ./entrypoint.sh

set -eu


host=$(echo "${POSTGRES_HOST}" | cut -d ":" -f1) # TODO: remove
./wait-for-db.sh "$host" "${POSTGRES_PORT:-5432}"

java -Duser.timezone=UTC -jar marquez-*.jar server "${MARQUEZ_CONFIG}"
