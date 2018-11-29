#!/bin/bash
#
# Usage: $ ./entrypoint.sh

set -eu

source common.sh

./wait-for-db.sh "${POSTGRES_HOST:-localhost}" "${POSTGRES_PORT:-5432}"

java -jar marquez-all.jar server "${MARQUEZ_CONFIG}"
