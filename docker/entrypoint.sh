#!/bin/bash
#
# Usage: $ ./entrypoint.sh

set -eu

./wait-for-it.sh "${POSTGRES_HOST}:${POSTGRES_PORT}" -s -t "${TIMEOUT}"  -- echo "postgres is up!"

java -jar marquez-all.jar server "${MARQUEZ_CONFIG}"
