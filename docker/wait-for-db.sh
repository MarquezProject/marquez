#!/bin/bash
#
# Usage: $ ./wait-for-db.sh <host> <port>

set -eu

source common.sh

host="${1}"
port="${2}"

until PGPASSWORD="${POSTGRES_PASSWORD}" psql \
        --host="${host}" \
        --port="${port}" \
        --username "${POSTGRES_USER}" \
        --dbname "${POSTGRES_DB}" \
        --command '\q' > /dev/null 2>&1; do
  info "Waiting for postgres to become available..."
  sleep 1
done

info "Great news! Postgres is up."
