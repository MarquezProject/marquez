#!/bin/bash
#
# Usage: $ ./login.sh

set -eu

echo "${DOCKER_PASSWD}" |docker login --username "${DOCKER_USER}" --password-stdin quay.io/wework
