#!/bin/bash
#
# Usage: $ ./build-tag-push.sh <version>

set -eu

source common.sh

readonly SEMVER_REGEX="^v[0-9]+\\.[0-9]+\\.[0-9]+$" # vX.Y.Z
readonly ORG="marquezproject"
readonly REPO="marquez"
readonly NAME="${ORG}/${REPO}"

project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

# Version X.Y.Z of Marquez image to build
version="${1}"

if [[ ! "${version}" =~ ${SEMVER_REGEX} ]]; then
  error "Version must match ${SEMVER_REGEX}"
fi

info "Building image (tag: ${version})..."

docker build --no-cache --tag "${NAME}:${version}" .
docker tag "${NAME}:${version}" "${NAME}:latest"
docker push "${NAME}:${version}"
docker push "${NAME}:latest"

info "DONE!"
