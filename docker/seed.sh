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

java -jar marquez-api-*.jar seed --host "${MARQUEZ_HOST:-localhost}" --port "${MARQUEZ_PORT:-5000}" "${MARQUEZ_CONFIG}"
