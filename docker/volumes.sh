#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

VOLUME_PREFIX=$1

UTILS_VOLUME="${VOLUME_PREFIX}_utils"
DB_INIT_VOLUME="${VOLUME_PREFIX}_db-init"

docker volume create $UTILS_VOLUME
docker volume create $DB_INIT_VOLUME
docker create --name marquez-volume-helper -v $UTILS_VOLUME:/opt/marquez-utils -v $DB_INIT_VOLUME:/opt/marquez-db-init busybox
docker cp ./docker/wait-for-it.sh marquez-volume-helper:/opt/marquez-utils/wait-for-it.sh
docker cp ./docker/init-db.sh marquez-volume-helper:/opt/marquez-db-init/init-db.sh

docker rm marquez-volume-helper
