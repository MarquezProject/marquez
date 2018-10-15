#!/bin/bash
#
# Usage: $ ./entrypoint.sh

set -eu

java -jar marquez-all.jar server $MARQUEZ_CONFIG
