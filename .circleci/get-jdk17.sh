#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./get-jdk17.sh

set -e

wget -qO - https://adoptium.jfrog.io/adoptium/api/gpg/key/public | sudo apt-key add -
sudo add-apt-repository --yes https://adoptium.jfrog.io/adoptium/deb
sudo apt-get update --allow-releaseinfo-change && sudo apt-get install --yes temurin-17-jdk
sudo update-alternatives --set java /usr/lib/jvm/temurin-17-jdk-amd64/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/temurin-17-jdk-amd64/bin/javac
java -version

echo "DONE!"
