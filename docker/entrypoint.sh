#!/bin/bash
#
# Usage: $ ./entrypoint.sh

set -eu

if [ -f ./wait-for-it.sh ]; then ./wait-for-it.sh "${POSTGRES_HOST}:5432"; fi

java -jar marquez-all.jar server "${MARQUEZ_CONFIG}"
