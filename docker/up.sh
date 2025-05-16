#!/usr/bin/env bash

# SPDX-License-Identifier: Apache-2.0
# Launch Marquez locally using Docker Compose
# Usage: ./up.sh [FLAGS]

set -euo pipefail

# Default versions and ports
VERSION="0.51.1"
TAG="${VERSION}"
BUILD="false"
SEED="false"
DETACH="false"
NO_WEB="false"
NO_SEARCH="false"
NO_VOLUMES="false"

API_PORT=5000
API_ADMIN_PORT=5001
WEB_PORT=3000
DB_PORT=5432
SEARCH_PORT=9200

COMPOSE_FILES="-f docker-compose.yml"
COMPOSE_ARGS="--remove-orphans"

show_usage() {
  cat <<EOF
Usage: ./up.sh [FLAGS] [OPTIONS]

Flags:
  -b, --build              Build Marquez from source
  -s, --seed               Seed the API with sample metadata
  -d, --detach             Run services in background
      --no-web             Disable the web UI
      --no-search          Disable the search container
      --no-volumes         Don't create Docker volumes
  -h, --help               Show this help message

Options:
  -t, --tag <ver>          Use specific image tag (default: ${VERSION})
  -a, --api-port <port>    API port (default: 5000)
  -m, --admin-port <port>  API admin port (default: 5001)
  -w, --web-port <port>    Web UI port (default: 3000)
  -p, --db-port <port>     DB port (default: 5432)
  -e, --search-port <port> Search port (default: 9200)
EOF
}

# Parse arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    -a|--api-port) shift; API_PORT="$1" ;;
    -m|--admin-port) shift; API_ADMIN_PORT="$1" ;;
    -w|--web-port) shift; WEB_PORT="$1" ;;
    -p|--db-port) shift; DB_PORT="$1" ;;
    -e|--search-port) shift; SEARCH_PORT="$1" ;;
    -t|--tag) shift; TAG="$1" ;;
    -b|--build) BUILD="true"; TAG="dev" ;;
    -s|--seed) SEED="true" ;;
    -d|--detach) DETACH="true" ;;
    --no-web) NO_WEB="true" ;;
    --no-search) NO_SEARCH="true" ;;
    --no-volumes) NO_VOLUMES="true" ;;
    -h|--help) show_usage; exit 0 ;;
    *) echo "Unknown option: $1"; show_usage; exit 1 ;;
  esac
  shift
done

# Set project root
PROJECT_ROOT="$(git rev-parse --show-toplevel)"
cd "$PROJECT_ROOT"

# Compose overrides
[[ "$BUILD" == "true" ]] && COMPOSE_FILES+=" -f docker-compose.dev.yml" && COMPOSE_ARGS+=" --build"
[[ "$SEED" == "true" ]] && COMPOSE_FILES+=" -f docker-compose.seed.yml"
[[ "$NO_WEB" == "false" ]] && COMPOSE_FILES+=" -f docker-compose.web.yml"
[[ "$BUILD" == "true" && "$NO_WEB" == "false" ]] && COMPOSE_FILES+=" -f docker-compose.web-dev.yml"
[[ "$NO_SEARCH" == "false" ]] && COMPOSE_FILES+=" -f docker-compose.search.yml"
[[ "$DETACH" == "true" ]] && COMPOSE_ARGS+=" --detach"

# Create volumes if not disabled
if [[ "$NO_VOLUMES" == "false" && -f ./docker/volumes.sh ]]; then
  ./docker/volumes.sh "$(basename "$PROJECT_ROOT")"
fi

# Toggle search
SEARCH_ENABLED="true"
[[ "$NO_SEARCH" == "true" ]] && SEARCH_ENABLED="false"

# Launch
echo -e "\n🚀 Starting Marquez ${TAG}..."

export API_PORT=${API_PORT}
export API_ADMIN_PORT=${API_ADMIN_PORT}
export WEB_PORT=${WEB_PORT}
export POSTGRES_PORT=${DB_PORT}
export SEARCH_PORT=${SEARCH_PORT}
export SEARCH_ENABLED=${SEARCH_ENABLED}
export TAG=${TAG}
docker compose $COMPOSE_FILES up $COMPOSE_ARGS
