#!/bin/bash
#
# Usage: $ ./seed-db.sh 

set -eu

# NAMESPACES

cat ./data/namespaces.json | jq -c '.[]' | \
  while read -r i; do
    namespace=$(echo "$i" | jq -r '.name')
    payload=$(echo "$i" | jq -c '{ownerName: .ownerName, description: .description}')  

    curl --silent -X PUT "http://${MARQUEZ_HOST}:${MARQUEZ_PORT}/api/v1/namespaces/${namespace}" \
      -H 'Content-Type: application/json' \
      -d "${payload}" \
    | jq .

  done

# SOURCES

cat ./data/sources.json | jq -c '.[]' | \
  while read -r i; do
    source=$(echo "$i" | jq -r '.name')
    payload=$(echo "$i" | jq -c '{type: .type, connectionUrl: .connectionUrl, description: .description}')  

    curl --silent -X PUT "http://${MARQUEZ_HOST}:${MARQUEZ_PORT}/api/v1/sources/${source}" \
      -H 'Content-Type: application/json' \
      -d "${payload}" \
    | jq .

  done

# DATASETS

cat ./data/datasets.json | jq -c '.[]' | \
  while read -r i; do
    namespace=$(echo "$i" | jq -r '.namespace')
    dataset=$(echo "$i" | jq -r '.name')
    payload=$(echo "$i" | jq -c '{type: .type, physicalName: .physicalName, sourceName: .sourceName, description: .description}')  

    curl --silent -X PUT "http://${MARQUEZ_HOST}:${MARQUEZ_PORT}/api/v1/namespaces/${namespace}/datasets/${dataset}" \
      -H 'Content-Type: application/json' \
      -d "${payload}" \
    | jq .

  done

# JOBS

cat ./data/jobs.json | jq -c '.[]' | \
  while read -r i; do
    namespace=$(echo "$i" | jq -r '.namespace')
    job=$(echo "$i" | jq -r '.name')
    payload=$(echo "$i" | jq -c '{type: .type, inputs: .inputs, outputs: .outputs, location: .location, description: .description}')  

    curl --silent -X PUT "http://${MARQUEZ_HOST}:${MARQUEZ_PORT}/api/v1/namespaces/${namespace}/jobs/${job}" \
      -H 'Content-Type: application/json' \
      -d "${payload}" \
    | jq .

  done

echo "DONE!"
