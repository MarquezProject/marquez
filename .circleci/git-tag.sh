#!/bin/bash
#
# A script used to tag a commit and kickoff CircleCI deploy workflow
#
# Release tag: VERSION.YYYYMMDD.SHA-1
#
# Usage: $ ./git-tag.sh

set -eu

# Change working directory .circleci
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/.circleci"

# Ensure on master branch
branch=$(git rev-parse --abbrev-ref HEAD)
if [[ "${branch}" != "master" ]]; then
  echo "You may only tag a commit on the 'master' branch"
  exit 1;
fi

# Get snapshot version (format: X.Y.Z-SNAPSHOT)
snapshot=$(grep 'version' ../gradle.properties | cut -d'=' -f2)

# Get upstream release tags only (descending order)
RELEASES=( $(git tag --sort=-creatordate | awk '/^[0-9]+(\.[0-9]+){2}$/') ) # X.Y.Z

# Get latest tagged release upstream and next release (snapshot)
latest_release="${RELEASES[0]}"
next_release="${snapshot%-*}" # Remove -SNAPSHOT

# To ensure we are referencing the latest stable release of Marquez upstream
# in relation to our mirrored repo, we perform the following checks when setting
# the version in our tag (i.e. VERSION.*):
#   (1) Default VERSION to latest stable version (ex: 0.1.0)
#   (2) Set VERSION to previous release iff latest and unreleased version are
#       equal (ex: 0.2.0 is equal to the unreleased version 0.2.0-SNAPSHOT,
#       therefore fallback to 0.1.0)
#   (3) Set VERSION to immediate preceding release relative to unreleased version
#       (ex: 0.3.0 is newer than 0.2.0-SNAPSHOT, therefore loop until we find 0.1.0)
release="${latest_release}"                             # (1)
if [[ "${release}" == "${next_release}" ]]; then        # (2)
  release="${RELEASES[1]}"
elif [[ "${release}" > "${next_release}" ]]; then       # (3)
  for i in "${!RELEASES[@]}"; do
   if [[ "${next_release}" == "${RELEASES[i]}" ]]; then
     release="${RELEASES[i + 1]}"
   fi
  done
fi

date=$(date +%Y%m%d)
sha=$(git log --pretty=format:'%h' -n 1)

# Tag in the format VERSION.YYYYMMDD.SHA-1 (ex: 0.1.0.20190213.a9e469e)
tag="${release}.${date}.${sha}"

# Tag commit
git tag -a "${tag}" -m "marquez ${tag}"

# Push tag to Github
git push origin "${tag}"

echo "Follow the status of the release here: 'https://circleci.com/gh/WeConnect/workflows/marquez'"
