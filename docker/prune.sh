#!/bin/bash
#
# Usage: $ ./prune.sh

set -e

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

docker image prune -a --filter "until=24h" --force
