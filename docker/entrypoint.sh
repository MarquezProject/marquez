#!/bin/bash
#
# Usage: $ ./entrypoint.sh

set -eu

./wait-for-it.sh "${POSTGRES_HOST}:5432" -t 300 -s -- echo "postgres is up!"

java -jar marquez-all.jar server "${MARQUEZ_CONFIG}"
