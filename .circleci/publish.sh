#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./publish.sh

set -e

# Get, then decode, the GPG private key used to sign *-SNAPSHOT.jar
export ORG_GRADLE_PROJECT_signingKey=$(echo $GPG_SIGNING_KEY | base64 -d)
# Publish *.jar
./gradlew publish

echo "DONE!"
