#!/bin/bash
#
# Usage: $ ./login.sh

set -eu

docker login -u "${DOCKER_LOGIN}" -p "${DOCKER_PASSWORD}"
