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

set -e

title() {
  echo -e "\033[1m${1}\033[0m"
}

usage() {
  echo "usage: ./$(basename -- ${0}) [--api-port PORT] [--web-port PORT] [--tag TAG] [--build] [--seed]"
  echo "A script used to run Marquez via Docker"
  echo
  title "EXAMPLES:"
  echo "  # Build image from source"
  echo "  $ ./up.sh --build"
  echo
  echo "  # Build image from source, then seed HTTP API server with metadata"
  echo "  $ ./up.sh --build --seed"
  echo
  echo "  # Use tagged image"
  echo "  ./up.sh --tag X.Y.X"
  echo
  echo "  # Use tagged image, then seed HTTP API server with metadata"
  echo "  ./up.sh --tag X.Y.X --seed"
  echo
  echo "  # Set HTTP API server port"
  echo "  ./up.sh --api-port 9000"
  echo
  title "ARGUMENTS:"
  echo "  -a, --api-port int          api port (default: 5000)"
  echo "  -m, --api-admin-port int    api admin port (default: 5001)"
  echo "  -w, --web-port int          web port (default: 3000)"
  echo "  -t, --tag string            image tag (default: latest)"
  echo
  title "FLAGS:"
  echo "  -b, --build           build images from source"
  echo "  -s, --seed            seed HTTP API server with metadata"
  echo "  -h, --help            show help for script"
  exit 1
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/"

compose_files="-f docker-compose.yml"
args="-V --force-recreate --remove-orphans"

API_PORT=5000
API_ADMIN_PORT=5001
WEB_PORT=3000
TAG=0.18.0
while [ $# -gt 0 ]; do
  case $1 in
    -a|'--api-port')
       shift
       API_PORT="${1}"
       ;;
    -m|'--api-admin-port')
       shift
       API_ADMIN_PORT="${1}"
       ;;
    -w|'--web-port')
       shift
       WEB_PORT="${1}"
       ;;
    -t|'--tag')
       shift
       TAG=0.18.0
       ;;
    -b|'--build')
       BUILD='true'
       TAG=0.18.0
       ;;
    -s|'--seed')
       SEED='true'
       ;;
    -h|'--help')
       usage
       exit 0
       ;;
    *) usage
       exit 1
       ;;
  esac
  shift
done

if [[ "${BUILD}" = "true" ]]; then
  compose_files+=" -f docker-compose.dev.yml"
  args+=" --build"
fi

if [[ "${SEED}" = "true" ]]; then
  compose_files+=" -f docker-compose.seed.yml"
fi

API_PORT=${API_PORT} API_ADMIN_PORT=${API_ADMIN_PORT} WEB_PORT=${WEB_PORT} TAG=0.18.0
