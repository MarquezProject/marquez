#!/bin/bash
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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
