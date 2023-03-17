#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

export INPUT_CHANGELOG_FILENAME=CHANGES.md
export GITHUB_REPOSITORY=MarquezProject/marquez

git clone --branch add-testing-script --single-branch git@github.com:merobi-hub/changelog-ci.git

python3 changelog-ci/scripts/main.py

rm -rf changelog-ci 