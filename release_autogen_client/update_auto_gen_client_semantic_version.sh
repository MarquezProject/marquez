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

setup_repos

export type=${1}
if [ -z "${type}" ]
then
  echo "defaulting to 'patch'"
  type="patch"
else
  echo "update type is: ${type}"
fi

version=$(python ${MARQUEZ_PYTHON_CLIENT_CODEGEN_CLONE_DIR}/setup.py --version)
refresh_codegen
