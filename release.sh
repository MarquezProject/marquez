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
# Usage: $ ./release.py <type>

set -e -x

branch=$(git rev-parse --abbrev-ref HEAD)

if [[ "${branch}" != "main" ]]; then
  echo "You may only tag a commit on the 'main' branch"
  exit 1;
fi

type=${1}
if [ -z "${type}" ]
then
  echo "defaulting to 'patch'"
  type="patch"
else
  echo "update type is: ${type}"
fi

version=$(python ./setup.py --version)  
echo "Upgrading version from the current version of ${version}"


# The {new_version} is not a bash variable - it's a bumpversion notation for the new version.
# Please see bumpversion --help for more information.
bumpversion --current-version ${version} \
  --search "VERSION = \"{current_version}\"" \
  --replace "VERSION = \"{new_version}\"" \
  --commit --tag \
  --tag-name {new_version} \
  ${type} ./setup.py
git push --tags origin main
echo "Done pushing to main"
