#!/bin/bash
#
# Usage: $ ./seed-db.sh 

set -eu

echo "Seeding database ..."

readonly RUN_STATES=('start' 'complete' 'fail' 'abort')
readonly RUNS=10

# NAMESPACES
cat ./data/namespaces.json | jq -c '.[]' | \
  while read -r i; do
    namespace=$(echo "${i}" | jq -r '.name')
    payload=$(echo "${i}" | jq -c '{ownerName: .ownerName, description: .description}')

    curl --silent --output /dev/null -X PUT "http://${MARQUEZ_HOST}:${MARQUEZ_PORT}/api/v1/namespaces/${namespace}" \
      -H 'Content-Type: application/json' \
      -d "${payload}"
  done

# SOURCES

cat ./data/sources.json | jq -c '.[]' | \
  while read -r i; do
    source=$(echo "${i}" | jq -r '.name')
    payload=$(echo "${i}" | jq -c '{type: .type, connectionUrl: .connectionUrl, description: .description}')

    curl --silent --output /dev/null -X PUT "http://${MARQUEZ_HOST}:${MARQUEZ_PORT}/api/v1/sources/${source}" \
      -H 'Content-Type: application/json' \
      -d "${payload}"
  done

# DATASETS

cat ./data/datasets.json | jq -c '.[]' | \
  while read -r i; do
    namespace=$(echo "${i}" | jq -r '.namespace')
    dataset=$(echo "${i}" | jq -r '.name')
    payload=$(echo "${i}" | jq -c '{type: .type, physicalName: .physicalName, sourceName: .sourceName, fields: .fields, description: .description}')

    curl --silent --output /dev/null -X PUT "http://${MARQUEZ_HOST}:${MARQUEZ_PORT}/api/v1/namespaces/${namespace}/datasets/${dataset}" \
      -H 'Content-Type: application/json' \
      -d "${payload}"
  done

# JOBS

cat ./data/jobs.json | jq -c '.[]' | \
  while read -r i; do
    namespace=$(echo "${i}" | jq -r '.namespace')
    job=$(echo "${i}" | jq -r '.name')
    payload=$(echo "${i}" | jq -c '{type: .type, inputs: .inputs, outputs: .outputs, location: .location, context: .context, description: .description}')

    curl --silent --output /dev/null -X PUT "http://${MARQUEZ_HOST}:${MARQUEZ_PORT}/api/v1/namespaces/${namespace}/jobs/${job}" \
      -H 'Content-Type: application/json' \
      -d "${payload}"

    if [[ "( $RANDOM % 2 )" -ge 0 ]]; then
      n=0
      runs=$(( $RANDOM % $RUNS ))
      while [[ "${n}" -lt $runs ]]; do
        response=$(curl --silent -X POST "http://${MARQUEZ_HOST}:${MARQUEZ_PORT}/api/v1/namespaces/${namespace}/jobs/${job}/runs" \
          -H 'Content-Type: application/json' \
          -d "{}")
        if [[ "( $RANDOM % 2 )" -ne 0 ]]; then
          run_id=$(echo "${response}" | jq -r '.runId')
          mark_run_as=${RUN_STATES[($RANDOM % 4)]}
          curl --silent --output /dev/null -X POST "http://${MARQUEZ_HOST}:${MARQUEZ_PORT}/api/v1/jobs/runs/${run_id}/${mark_run_as}"
        fi
        n=$((n + 1))
      done
    fi
  done

echo "DONE!"
