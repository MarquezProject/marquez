#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

REPO_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../../../ &> /dev/null && pwd )"
cd $REPO_DIR/integrations/common && pip wheel --wheel-dir=$REPO_DIR/examples/airflow/tmp/whl .
mkdir -p $REPO_DIR/examples/airflow/whl/
cp $REPO_DIR/examples/airflow/tmp/whl/marquez_integration_common* $REPO_DIR/examples/airflow/whl/
rm -rf $REPO_DIR/examples/airflow/tmp

cd $REPO_DIR/integrations/airflow && pip wheel -e $REPO_DIR/integrations/common --wheel-dir=$REPO_DIR/examples/airflow/whl .
ls $REPO_DIR/examples/airflow/whl | sed -e 's/^/\/whl\//' > $REPO_DIR/examples/airflow/requirements.txt