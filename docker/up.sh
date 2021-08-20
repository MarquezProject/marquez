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
# Usage: $ ./up.sh [--tag TAG] [--build] [--seed]

set -e

usage() {
  echo "usage: ./$(basename -- ${0}) [--tag TAG] [--build] [--seed]"
  echo
  echo "A script used to run Marquez via Docker"
  echo
  echo "Examples:"
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
  echo
  echo "Arguments:"
  echo "  -t, --tag string      image tag (default: latest)"
  echo "  -b, --build           build image from source"
  echo "  -s, --seed            seed HTTP API server with metadata"
  echo "  -h, --help            show help for script"
  exit 1
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

compose_files="-f docker-compose.yml"
args="-V --force-recreate"

TAG=0.16.1
while [ $# -gt 0 ]; do
  case $1 in
    '--tag'|-t)
       shift
       TAG="${1}"
       ;;
    '--build'|-b)
       BUILD='true'
       ;;
    '--seed'|-s)
       SEED='true'
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

if [[ "${BUILD}" = "true" ]]; then
  compose_files+=" -f docker-compose.dev.yml"
  args+=" --build"
fi

if [[ "${SEED}" = "true" ]]; then
  compose_files+=" -f docker-compose.seed.yml"
fi

TAG=${TAG} docker-compose $compose_files up $args
