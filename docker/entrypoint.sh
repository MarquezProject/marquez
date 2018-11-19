#!/bin/bash
#
# Usage: $ ./entrypoint.sh

set -eu

./wait-for-it.sh "${POSTGRES_HOST:-localhost}:${POSTGRES_PORT:-5432}" --timeout="${WAIT_TIMEOUT:-30}" --strict -- echo "postgres is up!"

java -jar marquez-all.jar server "${MARQUEZ_CONFIG}"
