#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Usage: $ ./new-version.sh <NEW_VERSION> <NEXT_VERSION>

set -e

usage() {
  echo "usage: ./$(basename -- ${0}) <NEW_VERSION> <NEXT_VERSION>"
  exit 1
}

readonly SEMVER_REGEX="^[0-9]+(\.[0-9]+){2}(-rc\.[0-9]+)?(-SNAPSHOT)?$" # X.Y.Z
                                                                        # X.Y.Z-rc.*
                                                                        # X.Y.Z-SNAPSHOT

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

# Verify bump2version is installed
if [[ ! $(type -P bump2version) ]]; then
  echo "bump2version not installed! Please see https://github.com/c4urself/bump2version#installation"
  exit 1
fi

branch=$(git symbolic-ref --short HEAD)
if [[ "${branch}" == "main" ]]; then
  echo "Error: You may only release on 'main'!"
  exit 1;
fi

if [[ $# -eq 0 ]] ; then
  usage
fi

NEW_VERSION="${1}"
NEXT_VERSION="${2}"

# Ensure valid versions
VERSIONS=($NEW_VERSION $NEXT_VERSION)
for VERSION in "${VERSIONS[@]}"; do
  if [[ ! "${VERSION}" =~ ${SEMVER_REGEX} ]]; then
    echo "Error: Version '${VERSION}' must match '${SEMVER_REGEX}'"
    exit 1
  fi
done

# (1) Bump python module versions
PYTHON_MODULES=(clients/python/ integrations/airflow/)
for PYTHON_MODULE in "${PYTHON_MODULES[@]}"; do
  (cd "${PYTHON_MODULE}" && bump2version manual --new-version "${NEW_VERSION}")
done

# (2) Bump java module versions
sed -i "" "s/version=.*/version=${NEW_VERSION}/g" gradle.properties

# (3) Prepare release commit
git commit -am "Prepare for release ${NEW_VERSION}"

# (4) Prepare release tag
git fetch --all --tags > /dev/null 2>&1
git tag -a "${NEW_VERSION}" -m "marquez ${NEW_VERSION}"

# (5) Prepare next development version
sed -i "" "s/version=.*/version=${NEXT_VERSION}/g" gradle.properties

# (6) Prepare next development version commit
git commit -am "Prepare next development version"

# (7) Push commits and tag
git push origin main && git push origin "${NEW_VERSION}"

echo "DONE!"
