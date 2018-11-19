#!/bin/bash
#
# Usage: $ ./entrypoint.sh

set -eu

if [ -f /usr/src/app/wait-for-it.sh ]; then /usr/src/app/wait-for-it.sh "${POSTGRES_HOST}:5432"; fi

java -jar marquez-all.jar server "${MARQUEZ_CONFIG}"
