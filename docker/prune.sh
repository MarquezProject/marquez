#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./prune.sh

set -e

docker image prune -a --filter "until=24h" --force
