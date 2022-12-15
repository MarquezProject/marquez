#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./db.sh [--backup | --migrate]

title() {
  echo -e "\033[1m${1}\033[0m"
}

usage() {
  echo "usage: ./$(basename -- ${0}) [--backup|--migrate]"
  echo "A script used to run db migrations"
  echo
  title "EXAMPLES:"
  echo "  # Backup db"
  echo "  $ ./db.sh --backup"
  echo
  echo "  # Migrate db"
  echo "  $ ./db.sh --migrate"
  echo
  title "FLAGS:"
  echo "  -b, --backup          backup db"
  echo "  -s, --migrate         backup db"
  echo "  -h, --help            show help for script"
  exit 1
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/"

while [ $# -gt 0 ]; do
  case $1 in
    -b|'--backup')
       BACKUP='true'
       ;;
    -m|'--migrate')
       MIGRATE='true'
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

# (1) ...
if [[ "${BACKUP}" = "true" ]]; then
  ./docker/up.sh \
    --args "-V --force-recreate --abort-on-container-exit --exit-code-from seed_marquez" \
    --tag "latest" \
    --seed
fi

if [[ "${MIGRATE}" = "true" ]]; then
  ./docker/up.sh \
    --args "--abort-on-container-exit --exit-code-from seed_marquez" \
    --build
fi

echo "DONE!"
