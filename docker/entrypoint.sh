#!/bin/bash
#
# Usage: $ ./entrypoint.sh

set -eu

./wait-for-it.sh \
  --host="${POSTGRES_HOST:-localhost}" \
  --port="${POSTGRES_PORT:-5432}" \
  --timeout="${WAIT_TIMEOUT:-30}" \
  --strict -- echo "Great news! Postgres is up."

java -jar marquez-all.jar server "${MARQUEZ_CONFIG}"
