#!/bin/bash
#
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./entrypoint.sh

set -e

if [[ -z "${MARQUEZ_CONFIG}" ]]; then
  MARQUEZ_CONFIG='marquez.dev.yml'
  echo "WARNING 'MARQUEZ_CONFIG' not set, using development configuration."
fi

# Start http server with configuration
java -Duser.timezone=UTC -Dlog4j2.formatMsgNoLookups=true -jar marquez-*.jar server "${MARQUEZ_CONFIG}"
