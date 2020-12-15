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
# Usage: $ ./up.sh [--pull]

set -e

usage() {
  echo "usage: ./$(basename -- ${0}) [--pull]"
  exit 1
}

# Change working directory to examples/airflow/
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/examples/airflow"

args="-V --force-recreate"

while [ $# -gt 0 ]; do
  case $1 in
    '--pull'|-p)
       PULL='true'
       ;;
    '--help'|-h)
       usage
       exit 0
       ;;
    *) usage
       exit 1
       ;;
  esac
  shift
done

if [[ "${PULL}" = "true" ]]; then
  docker-compose pull
fi

docker-compose up $args
