#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./db-migration.sh


# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/"

# (1) ...
./docker/up.sh \
  --args "-V --force-recreate --exit-code-from seed_marquez" \
  --tag "latest" \
  --seed

# (2) ...
./docker/up.sh \
  --args "--exit-code-from seed_marquez" \
  --build \
  --seed

echo "DONE!"
