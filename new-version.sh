#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Requirements:
#   * You're on the 'main' branch
#   * You've installed 'bump2version'
#   * You've installed 'redoc-cli'
#
# Usage: $ ./new-version.sh --release-version RELEASE_VERSION --next-version NEXT_VERSION

set -e

title() {
  echo -e "\033[1m${1}\033[0m"
}

usage() {
  echo "A script used to release Marquez"
  echo
  title "USAGE:"
  echo "  ./$(basename -- "${0}") --release-version RELEASE_VERSION --next-version NEXT_VERSION"
  echo
  title "EXAMPLES:"
  echo "  # Bump version ('-SNAPSHOT' will automatically be appended to '0.0.2')"
  echo "  $ ./new-version.sh -r 0.0.1 -n 0.0.2"
  echo
  echo "  # Bump version (with '-SNAPSHOT' already appended to '0.0.2')"
  echo "  $ ./new-version.sh -r 0.0.1 -n 0.0.2-SNAPSHOT"
  echo
  echo "  # Bump release candidate"
  echo "  $ ./new-version.sh -r 0.0.1-rc.1 -n 0.0.2-rc.2"
  echo
  echo "  # Bump release candidate without push"
  echo "  $ ./new-version.sh -r 0.0.1-rc.1 -n 0.0.2-rc.2 --no-push"
  echo
  title "ARGUMENTS:"
  echo "  -r, --release-version string    the release version (ex: X.Y.Z, X.Y.Z-rc.*)"
  echo "  -n, --next-version string       the next version (ex: X.Y.Z, X.Y.Z-SNAPSHOT)"
  echo
  title "FLAGS:"
  echo "  --no-push    local changes are not automatically pushed to the remote repository"
  exit 1
}

readonly SEMVER_REGEX="^[0-9]+(\.[0-9]+){2}((-rc\.[0-9]+)?(-SNAPSHOT)?)$" # X.Y.Z
                                                                          # X.Y.Z-rc.*
                                                                          # X.Y.Z-rc.*-SNAPSHOT
                                                                          # X.Y.Z-SNAPSHOT

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/"

# Verify bump2version is installed
if [[ ! $(type -P bump2version) ]]; then
  echo "bump2version not installed! Please see https://github.com/c4urself/bump2version#installation"
  exit 1;
fi

# Verify redoc-cli is installed
if [[ ! $(type -P redoc-cli) ]]; then
  echo "redoc-cli not installed! Please see https://redoc.ly/docs/redoc/quickstart/cli"
  exit 1;
fi

if [[ $# -eq 0 ]] ; then
  usage
fi

PUSH="true"
while [ $# -gt 0 ]; do
  case $1 in
    -r|--release-version)
       shift
       RELEASE_VERSION="${1}"
       ;;
    -n|--next-version)
       shift
       NEXT_VERSION="${1}"
       ;;
    --no-push)
       PUSH="false"
       ;;
    -h|--help)
       usage
       exit 0
       ;;
    *) usage
       exit 1
       ;;
  esac
  shift
done

branch=$(git symbolic-ref --short HEAD)
if [[ "${branch}" != "main" ]]; then
  echo "error: you may only release on 'main'!"
  exit 1;
fi

# Ensure no unstaged changes are present in working directory
if [[ -n "$(git status --porcelain --untracked-files=no)" ]] ; then
  echo "error: you have unstaged changes in your working directory!"
  exit 1;
fi

# Ensure valid versions
VERSIONS=($RELEASE_VERSION $NEXT_VERSION)
for VERSION in "${VERSIONS[@]}"; do
  if [[ ! "${VERSION}" =~ ${SEMVER_REGEX} ]]; then
    echo "Error: Version '${VERSION}' must match '${SEMVER_REGEX}'"
    exit 1
  fi
done

# Ensure python module version matches X.Y.Z or X.Y.ZrcN (see: https://www.python.org/dev/peps/pep-0440/),
PYTHON_RELEASE_VERSION=${RELEASE_VERSION}
if [[ "${RELEASE_VERSION}" == *-rc.? ]]; then
  RELEASE_CANDIDATE=${RELEASE_VERSION##*-}
  PYTHON_RELEASE_VERSION="${RELEASE_VERSION%-*}${RELEASE_CANDIDATE//.}"
fi

# (1) Bump java module versions
sed -i "" "s/version=.*/version=${RELEASE_VERSION}/g" gradle.properties

# (2) Bump version in helm chart
sed -i "" "s/^version:.*/version: ${RELEASE_VERSION}/g" ./chart/Chart.yaml
sed -i "" "s/tag:.*/tag: ${RELEASE_VERSION}/g" ./chart/values.yaml

# (3) Bump version in scripts
sed -i "" "s/TAG=\d.*/TAG=${RELEASE_VERSION}/g" ./docker/up.sh
sed -i "" "s/TAG=\d.*/TAG=${RELEASE_VERSION}/g" .env.example

# (4) Bump version in docs
sed -i "" "s/^  version:.*/  version: ${RELEASE_VERSION}/g" ./spec/openapi.yml
sed -i "" "s/<version>.*/<version>${RELEASE_VERSION}<\/version>/g" ./clients/java/README.md
sed -i "" "s/marquez-java:.*/marquez-java:${RELEASE_VERSION}/g" ./clients/java/README.md

# (5) Bundle openAPI docs
redoc-cli bundle spec/openapi.yml --output docs/openapi.html --title "Marquez API Reference"

# (6) Prepare release commit
git commit -sam "Prepare for release ${RELEASE_VERSION}" --no-verify

# (7) Pull latest tags, then prepare release tag
git fetch --all --tags
git tag -a "${RELEASE_VERSION}" -m "marquez ${RELEASE_VERSION}"

# (8) Prepare next development version for python and java modules
PYTHON_MODULES=(clients/python/)
for PYTHON_MODULE in "${PYTHON_MODULES[@]}"; do
  (cd "${PYTHON_MODULE}" && bump2version manual --new-version "${NEXT_VERSION}" --allow-dirty)
done
# For Java modules, append '-SNAPSHOT' to 'NEXT_VERSION' if a release candidate, or missing
# (ex: '-SNAPSHOT' will be appended to X.Y.Z or X.Y.Z-rc.N)
if [[ "${NEXT_VERSION}" == *-rc.? ||
      ! "${NEXT_VERSION}" == *-SNAPSHOT ]]; then
  NEXT_VERSION="${NEXT_VERSION}-SNAPSHOT"
fi
sed -i "" "s/version=.*/version=${NEXT_VERSION}/g" gradle.properties
sed -i "" "s/^  version:.*/  version: ${NEXT_VERSION}/g" ./spec/openapi.yml

# (9) Prepare next development version commit
git commit -sam "Prepare next development version ${NEXT_VERSION}" --no-verify

# (10) Check for commits in log
COMMITS=false
MESSAGE_1=$(git log -1 --grep="Prepare for release ${RELEASE_VERSION}" --pretty=format:%s)
MESSAGE_2=$(git log -1 --grep="Prepare next development version ${NEXT_VERSION}" --pretty=format:%s)

if [[ $MESSAGE_1 ]] && [[ $MESSAGE_2 ]]; then
  COMMITS=true
else
  echo "one or both commits failed; exiting..."
  exit 0
fi

# (11) Push commits and tag
if [[ $COMMITS = "true" ]] && [[ ${PUSH} = "true" ]]; then
  git push origin main && \
    git push origin "${RELEASE_VERSION}"
else
  echo "...skipping push to 'main'; to push commits manually, run:"
  echo "    $ git push origin main && git push origin "${RELEASE_VERSION}""
fi

echo "DONE!"
