#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./seed.sh

set -e

# As ISO-8601 format
NOW=$(date -u +"%Y-%m-%dT%H:%M:%S.000Z")

RUN_END_TIME_AFTER_5_MINUTES=$(date -u -d "5 minutes" +"%Y-%m-%dT%H:%M:%S.000Z")
RUN_END_TIME_AFTER_6_MINUTES=$(date -u -d "6 minutes" +"%Y-%m-%dT%H:%M:%S.000Z")
RUN_END_TIME_AFTER_7_MINUTES=$(date -u -d "7 minutes" +"%Y-%m-%dT%H:%M:%S.000Z")
RUN_END_TIME_AFTER_8_MINUTES=$(date -u -d "8 minutes" +"%Y-%m-%dT%H:%M:%S.000Z")
RUN_END_TIME_AFTER_9_MINUTES=$(date -u -d "9 minutes" +"%Y-%m-%dT%H:%M:%S.000Z")
RUN_END_TIME_AFTER_10_MINUTES=$(date -u -d "10 minutes" +"%Y-%m-%dT%H:%M:%S.000Z")

# Replace '{{RUN_START_TIME}}' and '{{RUN_END_TIME_AFTER_*_MINUTES}}'.
sed -e "s/{{RUN_START_TIME}}/$NOW/" \
    -e "s/{{RUN_END_TIME_AFTER_5_MINUTES}}/$RUN_END_TIME_AFTER_5_MINUTES/" \
    -e "s/{{RUN_END_TIME_AFTER_6_MINUTES}}/$RUN_END_TIME_AFTER_6_MINUTES/" \
    -e "s/{{RUN_END_TIME_AFTER_7_MINUTES}}/$RUN_END_TIME_AFTER_7_MINUTES/" \
    -e "s/{{RUN_END_TIME_AFTER_8_MINUTES}}/$RUN_END_TIME_AFTER_8_MINUTES/" \
    -e "s/{{RUN_END_TIME_AFTER_9_MINUTES}}/$RUN_END_TIME_AFTER_9_MINUTES/" \
    -e "s/{{RUN_END_TIME_AFTER_10_MINUTES}}/$RUN_END_TIME_AFTER_10_MINUTES/" \
    metadata.template.json > metadata.json

java -jar marquez-api-*.jar seed --url "${MARQUEZ_URL:-http://localhost:5000}" --metadata metadata.json
