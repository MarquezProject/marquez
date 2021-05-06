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
# Requirements:
#   * You're on the 'main' branch
#   * You've installed 'bump2version'
#
# Usage: $ ./new-version.sh --release-version RELEASE_VERSION --next-version NEXT_VERSION

set -e

usage() {
  echo "Usage: ./$(basename -- "${0}") --release-version RELEASE_VERSION --next-version NEXT_VERSION"
  echo
  echo "A script used to release Marquez"
  echo
  echo "Examples:"
  echo "  # Bump version ('-SNAPSHOT' will automatically be appended to '0.0.2')"
  echo "  $ ./new-version.sh -r 0.0.1 -n 0.0.2"
  echo
  echo "  # Bump version (with '-SNAPSHOT' already appended to '0.0.2')"
  echo "  $ ./new-version.sh -r 0.0.1 -n 0.0.2-SNAPSHOT"
  echo
  echo "  # Bump release candidate"
  echo "  $ ./new-version.sh -r 0.0.1-rc.1 -n 0.0.2-rc.2"
  echo
  echo "Arguments:"
  echo "  -r, --release-version string       the release version (ex: X.Y.Z, X.Y.Z-rc.*)"
  echo "  -n, --next-version string          the next version (ex: X.Y.Z, X.Y.Z-SNAPSHOT)"
  exit 1
}

readonly SEMVER_REGEX="^[0-9]+(\.[0-9]+){2}((-rc\.[0-9]+)?|(-SNAPSHOT)?)$" # X.Y.Z
                                                                           # X.Y.Z-rc.*
                                                                           # X.Y.Z-SNAPSHOT

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

# Verify bump2version is installed
if [[ ! $(type -P bump2version) ]]; then
  echo "bump2version not installed! Please see https://github.com/c4urself/bump2version#installation"
  exit 1;
fi

branch=$(git symbolic-ref --short HEAD)
if [[ "${branch}" != "main" ]]; then
  echo "error: you may only release on 'main'!"
  exit 1;
fi

if [[ $# -eq 0 ]] ; then
  usage
fi

# check if we have either staged or unstaged changes in working tree
if [[ -n "$(git status --porcelain)" ]] ; then
  echo "error: you have changes in your working tree"
  exit 1;
fi

while [ $# -gt 0 ]; do
  case $1 in
    '--release-version'|-r)
       shift
       RELEASE_VERSION="${1}"
       ;;
    '--next-version'|-n)
       shift
       NEXT_VERSION="${1}"
       ;;
    '--help'|-h)
       usage
       ;;
    *) exit 1
       ;;
  esac
  shift
done

# Append '-SNAPSHOT' to 'NEXT_VERSION' if not a release candidate, or missing
if [[ ! "${NEXT_VERSION}" == *-rc.? &&
      ! "${NEXT_VERSION}" == *-SNAPSHOT ]]; then
  NEXT_VERSION="${NEXT_VERSION}-SNAPSHOT"
fi

# Ensure valid versions
VERSIONS=($RELEASE_VERSION $NEXT_VERSION)
for VERSION in "${VERSIONS[@]}"; do
  if [[ ! "${VERSION}" =~ ${SEMVER_REGEX} ]]; then
    echo "Error: Version '${VERSION}' must match '${SEMVER_REGEX}'"
    exit 1
  fi
done

# (1) Bump python module versions
PYTHON_MODULES=(clients/python/ integrations/airflow/)
for PYTHON_MODULE in "${PYTHON_MODULES[@]}"; do
  (cd "${PYTHON_MODULE}" && bump2version manual --new-version "${RELEASE_VERSION}" --allow-dirty)
done

# (2) Bump java module versions
sed -i "" "s/version=.*/version=${RELEASE_VERSION}/g" gradle.properties

# (3) Prepare release commit
git commit -sam "Prepare for release ${RELEASE_VERSION}"

# (4) Pull latest tags, then prepare release tag
git fetch --all --tags
git tag -a "${RELEASE_VERSION}" -m "marquez ${RELEASE_VERSION}"

# (5) Prepare next development version
sed -i "" "s/version=.*/version=${NEXT_VERSION}/g" gradle.properties

# (6) Prepare next development version commit
git commit -sam "Prepare next development version"

# (7) Push commits and tag
git push origin main && git push origin "${RELEASE_VERSION}"

echo "DONE!"
