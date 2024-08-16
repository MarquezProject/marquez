#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./volumes.sh <volume-prefix>

title() {
  echo -e "\033[1m${1}\033[0m"
}

ls() {
  docker run -it --rm -v "${1}:/tmp" busybox ls /tmp
}

usage() {
  echo "usage: ./$(basename -- ${0}) <volume-prefix>"
  echo "A script used to create persistent volumes for Marquez"
  echo
  title "EXAMPLE:"
  echo "  # Create volumes with prefix"
  echo "  $ ./volumes.sh my-volume-prefix"
  exit 1
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}/"

# Volume prefix for Marquez
volume_prefix="${1}"

# Ensure valid volume prefix
if [[ -z "${volume_prefix}" ]]; then
  echo "Volume prefix must not be empty or blank!"
  usage
fi

# Volumes with prefix
data_volume="${volume_prefix}_data"
db_conf_volume="${volume_prefix}_db-conf"
db_init_volume="${volume_prefix}_db-init"
db_backup_volume="${volume_prefix}_db-backup"
opensearch_volume="${volume_prefix}_opensearch-data"

echo "...creating volumes: ${data_volume}, ${db_conf_volume}, ${db_init_volume}, ${db_backup_volume} ${opensearch_volume}"

# Create persistent volumes for Marquez
docker volume create "${data_volume}" > /dev/null
docker volume create "${db_conf_volume}" > /dev/null
docker volume create "${db_init_volume}" > /dev/null
docker volume create "${db_backup_volume}" > /dev/null
docker volume create "${opensearch_volume}" > /dev/null

# Provision persistent volumes for Marquez
docker create --name volumes-provisioner \
  -v "${data_volume}:/data" \
  -v "${db_conf_volume}:/db-conf" \
  -v "${db_init_volume}:/db-init" \
  busybox > /dev/null 2>&1

# Add startup configuration for Marquez
docker cp ./docker/wait-for-it.sh volumes-provisioner:/data/wait-for-it.sh
echo "Added files to volume ${data_volume}: $(ls "${data_volume}")"

# Add db configuration
docker cp ./docker/postgresql.conf volumes-provisioner:/db-conf/postgresql.conf
echo "Added files to volume ${db_conf_volume}: $(ls "${db_conf_volume}")"

# Add db scripts
docker cp ./docker/init-db.sh volumes-provisioner:/db-init/init-db.sh
echo "Added files to volume ${db_init_volume}: $(ls "${db_init_volume}")"

# Delete volumes provisioner for Marquez
docker rm volumes-provisioner > /dev/null

echo "DONE!"
