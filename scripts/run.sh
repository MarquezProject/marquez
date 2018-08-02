#!/bin/bash

set -e

cd $(dirname $0)/..

./gradlew shadowJar
java -jar build/libs/marquez-all.jar server config.yaml
