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
# Usage: $ ./update_semantic_version_autogen_client.sh [ major | minor | patch ]
# This script will update the semantic versioning for a client
# based on the type specified. Default is 'patch'
set -e
set +x

source ./common_packaging_utils.sh

export new_version=${1}
echo "new_version is ${1}"
if [ -z "${new_version}" ]; then
  echo "required parameter 'version' not provided. Please specify in form {major}.{minor}.{patch}"
  exit 1
fi

verify_bumpversion_installed
setup_repos
refresh_codegen
