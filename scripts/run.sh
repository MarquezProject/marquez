#!/bin/bash

set -e

cd $(dirname $0)/..

# Build marquez
./gradlew shadowJar

# Start marquez server
java -jar build/libs/marquez-all.jar server config.yaml
