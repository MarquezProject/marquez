#!/bin/bash
#
# Usage: $ ./build-tag-push.sh <version>

set -eu

declare -r org=projectmarquez
declare -r repo=marquez

project_root=$(git rev-parse --show-toplevel)
cd $project_root/docker

# Version of Marquez image to build
version=$1

docker build --no-cache -t ${org}/${repo}:${version} .
docker tag ${org}/${repo}:${version} ${org}/${repo}:latest
docker push ${org}/${repo}:${version}
docker push ${org}/${repo}:latest
