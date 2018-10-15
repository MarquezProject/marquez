#!/bin/bash
#
# Usage: $ ./build-tag-push.sh <version>

set -eu

readonly ORG=projectmarquez
readonly REPO=marquez

project_root=$(git rev-parse --show-toplevel)
cd $project_root/docker

# Version of Marquez image to build
version=$1

echo "Building image (tag: ${version})..."

docker build --no-cache -t ${ORG}/${REPO}:${version} .
docker tag ${ORG}/${REPO}:${version} ${ORG}/${REPO}:latest
docker push ${ORG}/${REPO}:${version}
docker push ${ORG}/${REPO}:latest

echo "DONE!"
