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

readonly SEMVER_REGEX="^[0-9]+(\.[0-9]+){2}$" # X.Y.Z
readonly ORG="marquezproject"
readonly REPO="marquez"
readonly NAME="${ORG}/${REPO}"

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

# Version X.Y.Z of Marquez image to build
version="${1}"

# Ensure valid version 
if [[ ! "${version}" =~ ${SEMVER_REGEX} ]]; then
  echo "Version must match ${SEMVER_REGEX}"
  exit 1
fi

echo "Building image (tag: ${version})..."

# Build and tag image
docker build --no-cache --tag "${NAME}:${version}" .
docker tag "${NAME}:${version}" "${NAME}:latest"

# Push image to Docker Hub
docker push "${NAME}:${version}"
docker push "${NAME}:latest"

echo "DONE!"
