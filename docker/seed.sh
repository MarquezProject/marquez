#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./seed.sh

set -e

if [[ -z "${MARQUEZ_CONFIG}" ]]; then
  MARQUEZ_CONFIG='marquez.dev.yml'
  echo "WARNING 'MARQUEZ_CONFIG' not set, using development configuration."
fi

java -jar marquez-api-*.jar seed --url "${MARQUEZ_URL:-http://localhost:5000}" --metadata metadata.json "${MARQUEZ_CONFIG}"
