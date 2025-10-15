#!/usr/bin/env bash
#   Use this script to test if a given TCP host/port are available

# The MIT License (MIT)
# Copyright (c) 2016 Casey Hissam
# https://github.com/vishnubob/wait-for-it

set -e

TIMEOUT=15
QUIET=0
HOST=""
PORT=""

usage() {
  echo "Usage: $0 host:port [-t timeout] [-- command args]"
  echo "  -t TIMEOUT  Timeout in seconds, zero for no timeout (default: 15)"
  echo "  -- QUIET    Do not output any status messages"
  exit 1
}

wait_for() {
  if [[ $TIMEOUT -gt 0 ]]; then
    echo "Waiting $TIMEOUT seconds for $HOST:$PORT"
  else
    echo "Waiting for $HOST:$PORT without a timeout"
  fi

  start_ts=$(date +%s)

  while :
  do
    if nc -z "$HOST" "$PORT" >/dev/null 2>&1; then
      echo "$HOST:$PORT is available"
      break
    fi
    sleep 1

    if [[ $TIMEOUT -gt 0 ]]; then
      now_ts=$(date +%s)
      if (( now_ts - start_ts >= TIMEOUT )); then
        echo "Timeout occurred after waiting $TIMEOUT seconds for $HOST:$PORT"
        exit 1
      fi
    fi
  done
}

# Parse arguments
while [[ $# -gt 0 ]]
do
  case "$1" in
    *:* )
    HOST=$(echo "$1" | cut -d':' -f1)
    PORT=$(echo "$1" | cut -d':' -f2)
    shift 1
    ;;
    -t)
    TIMEOUT="$2"
    shift 2
    ;;
    --quiet)
    QUIET=1
    shift 1
    ;;
    --)
    shift
    break
    ;;
    *)
    usage
    ;;
  esac
done

if [[ -z "$HOST" || -z "$PORT" ]]; then
  usage
fi

if [[ $QUIET -eq 0 ]]; then
  wait_for
else
  wait_for >/dev/null 2>&1
fi

exec "$@"
