#!/bin/bash
#
# Usage: $ ./git-tag.sh

set -eu

readonly GITHUB_BASE_URL="https://api.github.com"
readonly OWNER="MarquezProject"
readonly REPO="marquez"

project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/.circleci"

snapshot=$(grep 'version' ../gradle.properties | cut -d'=' -f2)

TAG_URLS=( $(curl -s "${GITHUB_BASE_URL}/repos/${OWNER}/${REPO}/git/refs/tags" | jq -r '.[].object.url') )
TAGS=()
for i in "${!TAG_URLS[@]}"; do
  TAGS+=( $(curl -s "${TAG_URLS[i]}" | jq -r '.tag') )
done

IFS=$'\n'
RELEASES=( $(sort -r <<< "${TAGS[*]}") )

latest_release=$(echo "${RELEASES[0]}")
next_release="${snapshot%-*}"

release="${latest_release}"
if [[ "${release}" == "${next_release}" ]]; then
  release="${RELEASES[1]}"
elif [[ "${release}" > "${next_release}" ]]; then
  for i in "${!RELEASES[@]}"; do
   if [[ "${next_release}" == "${RELEASES[i]}" ]]; then
     release="${RELEASES[i + 1]}"
   fi
  done
fi

date=$(date +%Y%m%d)
sha=$(git log --pretty=format:'%h' -n 1)
tag="${release}.${date}.${sha}"

git tag -a "${tag}" -m "marquez ${tag}"
git push origin "${tag}"
