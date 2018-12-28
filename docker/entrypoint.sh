#!/bin/bash
#
# Usage: $ ./entrypoint.sh

set -eu

./wait-for-db.sh "${POSTGRES_HOST:-localhost}" "${POSTGRES_PORT:-5432}"

java -jar marquez-all.jar server "${MARQUEZ_CONFIG}"
