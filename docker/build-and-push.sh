#!/bin/bash
#
# Usage: $ ./build-tag-push.sh <version>

set -eu

# X.Y.Z
readonly SEMVER_REGEX=/^\d+\.\d+\.\d+$/
# Docker Hub repository
readonly ORG=projectmarquez
readonly REPO=marquez

project_root=$(git rev-parse --show-toplevel)
cd $project_root

# Version of Marquez image to build
version=$1

if [[ ! $version =~ $SEMVER_REGEX ]]; then
  echo "ERROR: Version must match ${SEMVER_REGEX}"
  exit 1
fi

echo "Building image (tag: ${version})..."

docker build --no-cache -t ${ORG}/${REPO}:${version} .
docker tag ${ORG}/${REPO}:${version} ${ORG}/${REPO}:latest
docker push ${ORG}/${REPO}:${version}
docker push ${ORG}/${REPO}:latest

echo "DONE!"
