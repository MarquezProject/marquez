#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./load-test.sh

set -e


usage() {
  echo "usage: ./$(basename -- ${0}) [--vus AMOUNT] [--docker]"
  echo "A script used to load test Marquez"
  echo
  title "EXAMPLES:"
  echo "  # Run load tests with 20 processes"
  echo "  $ ./load-test.sh --vus 20"
  echo
  title "ARGUMENTS:"
  echo "  -v, --vus           amount of load testing processes (default: 10)"
  echo
  title "FLAGS:"
  echo "  -d, --docker        run Marquez in docker"
  exit 1
}

VUS=10
LEVELS=3

while [ $# -gt 0 ]; do
  case $1 in
    -v|'--vus')
       shift
       VUS="${1}"
       ;;
    -d|'--docker')
       DOCKER='true'
       ;;
    -h|'--help')
       usage
       exit 0
       ;;
    *) usage
       exit 1
       ;;
  esac
  shift
done

DATASETS=$(expr $VUS + $LEVELS)

./gradlew clean shadowJar

java -jar api/build/libs/*.jar graph --levels 3 --datasets $DATASETS --first-level 3 --output graph.json

if [[ "${DOCKER}" = "true" ]]; then
  ./docker/up.sh --build --detach
fi

sleep 10

k6 run --vus $VUS --duration 30s load.js

if [[ "${DOCKER}" = "true" ]]; then
  docker-compose stop
fi
