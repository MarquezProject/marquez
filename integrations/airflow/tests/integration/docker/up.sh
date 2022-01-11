#!/bin/bash
#
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./up.sh

set -e

# Change working directory to integration
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"/integrations/airflow/tests/integration

if [[ "$(docker images -q marquez-airflow-base:latest 2> /dev/null)" == "" ]]; then
  echo "Please run 'docker build -f Dockerfile.tests -t marquez-airflow-base .' at base folder"
  exit 1
fi

# maybe overkill
MARQUEZ_AIRFLOW_WHL=$(docker run marquez-airflow-base:latest sh -c "ls /whl/marquez*")
MARQUEZ_AIRFLOW_WHL_ALL=$(docker run marquez-airflow-base:latest sh -c "ls /whl/*")

# Add revision to requirements.txt
echo "${MARQUEZ_AIRFLOW_WHL_ALL}" > requirements.txt

# Add revision to integration-requirements.txt
cat > integration-requirements.txt <<EOL
apache-airflow==1.10.12
marquez-python
psycopg2-binary==2.8.6
retrying==1.3.3
${MARQUEZ_AIRFLOW_WHL}
EOL

docker-compose up --build --force-recreate --exit-code-from integration
