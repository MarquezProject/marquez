#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
#
# Usage: $ ./get-jdk17.sh

set -e

# Eclipse Temurin 17.0.21.1+2 contains the fix for CVE-2026-114938.
TEMURIN_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.21.1%2B2/OpenJDK17U-jdk_x64_linux_hotspot_17.0.21.1_2.tar.gz"
TEMURIN_SHA256="0b0ac5eab6db6abaec68c9913bcddb31321e5da411c45073d520d9c125e6f7b4"
INSTALL_DIR="/usr/lib/jvm/temurin-17.0.21.1+2"

tmp="$(mktemp)"
curl -fsSL -o "${tmp}" "${TEMURIN_URL}"
echo "${TEMURIN_SHA256}  ${tmp}" | sha256sum -c -
sudo mkdir -p "${INSTALL_DIR}"
sudo tar -xzf "${tmp}" -C "${INSTALL_DIR}" --strip-components=1
rm -f "${tmp}"

sudo update-alternatives --install /usr/bin/java java "${INSTALL_DIR}/bin/java" 1702112
sudo update-alternatives --install /usr/bin/javac javac "${INSTALL_DIR}/bin/javac" 1702112
sudo update-alternatives --set java "${INSTALL_DIR}/bin/java"
sudo update-alternatives --set javac "${INSTALL_DIR}/bin/javac"
java -version

echo "DONE!"
