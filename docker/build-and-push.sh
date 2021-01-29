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

# Build and tag app image
docker build --no-cache --tag "marquez:${version}" .
docker tag 'marquez' 'marquez:latest'

# Build and tag web image
docker build --no-cache --tag "marquez-web:${version}" .
docker tag 'marquez-web' 'marquez-web:latest'

# Push images to Docker Hub
docker push "marquez:${version}"
docker push 'marquez:latest'
docker push "marquez-web:${version}"
docker push 'marquez-web:latest'

echo "DONE!"
