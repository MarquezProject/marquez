#!/bin/bash
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
set -e -x

# Usage: $ ./prepare_for_release.py <type>
# type - [major | minor | patch]. Default is patch.

# Verify bump2version is installed
if [[ ! $(type -P bump2version) ]]; then
 echo "bump2version not installed! Please see https://github.com/c4urself/bump2version#installation"
 exit 1
fi

branch=$(git rev-parse --abbrev-ref HEAD)
if [[ "${branch}" != "master" ]]; then
  echo "You may only tag a commit on the 'master' branch"
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


# The {new_version} is not a bash variable - it's a bump2version notation for the new version.
# Please see bump2version --help for more information.
bump2version --current-version ${version} --commit --tag --tag-name {new_version} ${type} ./setup.py
git push --tags origin master
echo "Done pushing to master"
