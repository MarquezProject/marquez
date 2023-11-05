#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./down.sh

set -e

title() {
  echo -e "\033[1m${1}\033[0m"
}

usage() {
  echo "usage: ./$(basename -- ${0})"
  echo "A script used to bring down Marquez when run via Docker"
  echo "If not using Docker Compose, use -v or --volumes"
  echo
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/"

compose_files="-f docker-compose.yml"
compose_args="--remove-orphans"

# Default args
NO_COMPOSE="false"

# Parse args
while [ $# -gt 0 ]; do
  case $1 in
    -v|'--volumes')
       shift
       NO_COMPOSE="true"
       ;;
    -h|'--help')
       usage
       exit 0
       ;;
  esac
  shift
done

# We can ignore the tag and port(s) when cleaning up running
# containers and volumes
TAG=any

API_PORT=${RANDOM}
API_ADMIN_PORT=${RANDOM}
WEB_PORT=${RANDOM}
TAG=${RANDOM}

if [[ "${NO_COMPOSE}" = "false" ]]; then
  docker-compose $compose_files down $compose_args
fi

docker volume rm marquez_data && \
  docker volume rm marquez_db-backup && \
  docker volume rm marquez_db-conf && \
  docker volume rm marquez_db-init
