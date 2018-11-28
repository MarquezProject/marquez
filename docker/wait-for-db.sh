#!/bin/bash
#
# Usage: $ wait-for-db.sh

set -eu

source common.sh

host="$1"
port="$2"

until PGPASSWORD="${POSTGRES_PASSWORD}" psql 
        --host="${host}" \
        --port="${port}" \
        --no-password \
        --username "${POSTGRES_USER}" \
        --commnad '\q'; do
  info "Waiting for postgres to become available..."
  sleep 1
done

info "Great news! Postgres is up."
