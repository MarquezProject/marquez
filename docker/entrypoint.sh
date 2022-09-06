#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./entrypoint.sh

set -e

if [[ -z "${MARQUEZ_CONFIG}" ]]; then
  MARQUEZ_CONFIG='marquez.dev.yml'
  echo "WARNING 'MARQUEZ_CONFIG' not set, using development configuration."
fi

if [[ -z "${MARQUEZ_VERSION}" ]]; then
  MARQUEZ_VERSION='*'
  echo "WARNING 'MARQUEZ_VERSION' not set. Running could fail if directory contains multiple jar versions."
fi


# Adjust java options for the http server
JAVA_OPTS="${JAVA_OPTS} -Duser.timezone=UTC -Dlog4j2.formatMsgNoLookups=true"

# Start http server with java options and configuration
java ${JAVA_OPTS} -jar marquez-${MARQUEZ_VERSION}.jar server ${MARQUEZ_CONFIG}
