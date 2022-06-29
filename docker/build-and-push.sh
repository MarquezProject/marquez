#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./build-and-push.sh <version>

set -eu

readonly SEMVER_REGEX="^[0-9]+(\.[0-9]+){2}(-rc\.[0-9]+)?$" # X.Y.Z
readonly ORG="marquezproject"

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

# Version X.Y.Z of Marquez images to build
version="${1}"

# Ensure valid version 
if [[ ! "${version}" =~ ${SEMVER_REGEX} ]]; then
  echo "Version must match ${SEMVER_REGEX}"
  exit 1
fi

echo "Building images (tag: ${version})..."

# Build, tag and push app image
docker build --no-cache --tag "${ORG}/marquez:${version}" .
docker tag "${ORG}/marquez:${version}" "${ORG}/marquez:latest"

docker push "${ORG}/marquez:${version}"
docker push "${ORG}/marquez:latest"

# Change working directory to web module
cd "${project_root}"/web

# Build, tag and push web image
docker build --no-cache --tag "${ORG}/marquez-web:${version}" .
docker tag "${ORG}/marquez-web:${version}" "${ORG}/marquez-web:latest"

docker push "${ORG}/marquez-web:${version}"
docker push "${ORG}/marquez-web:latest"

echo "DONE!"
