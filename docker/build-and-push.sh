#!/bin/bash
#
# Usage: $ ./build-and-push.sh <version>

set -eu

readonly SEMVER_REGEX="^[0-9]+(\.[0-9]+){2}\.([0-9]){4}([0-9]){2}([0-9]){2}\.([0-9a-f]){7}$" # X.Y.Z.YYYMMDD.SHA-1
readonly ORG="wework"
readonly REPO="marquez"
readonly NAME="${ORG}/${REPO}"

project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

# Version X.Y.Z of Marquez image to build
version="${1}"

if [[ ! "${version}" =~ ${SEMVER_REGEX} ]]; then
  error "Version must match ${SEMVER_REGEX}"
fi

echo "Building image (tag: ${version})..."

docker build --no-cache --tag "quay.io/${NAME}:${version}" .
docker tag "quay.io/${NAME}:${version}" "quay.io/${NAME}:latest"
docker push "quay.io/${NAME}:${version}"
docker push "quay.io/${NAME}:latest"

echo "DONE!"
