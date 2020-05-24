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
# Usage: $ ./up.sh [--local]

set -e

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

compose_files="-f docker-compose.yml"
args="-V --force-recreate"

if [ "${1}" = "--local" ]; then
  compose_files+=" -f docker-compose.local.yml"
  args+=" --build"
fi

docker-compose down && docker-compose $compose_files up $args
