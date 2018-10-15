#!/bin/bash
#
# Usage: $ ./build-tag-push.sh <version>

set -eu

project_root=$(git rev-parse --show-toplevel)
cd $project_root/docker

# Version of Marquez image to build
version=$1

docker build --no-cache -t projectmarquez/marquez:${version} .
docker tag projectmarquez/marquez:${version} projectmarquez/marquez:latest
docker push projectmarquez/marquez:${version}
docker push projectmarquez/marquez:latest
