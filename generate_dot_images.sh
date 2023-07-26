#!/bin/bash
#
# Copyright 2018-2023 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

for l in `ls docs/assets/dot/*.dot`; do
  name=$(basename $l)
  docker run -v ${PWD}/docs/assets/dot:/graphs -i fgrehm/graphviz dot -Tpng -O /graphs/$name
done
