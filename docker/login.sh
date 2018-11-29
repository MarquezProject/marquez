#!/bin/bash
#
# Usage: $ ./login.sh

set -eu

docker login --username "${DOCKER_LOGIN}" --password "${DOCKER_PASSWORD}"
