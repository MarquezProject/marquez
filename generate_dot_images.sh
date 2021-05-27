#!/bin/bash

for l in `ls docs/assets/dot/*.dot`; do
  name=$(basename $l)
  docker run -v ${PWD}/docs/assets/dot:/graphs -i fgrehm/graphviz dot -Tpng -O /graphs/$name
done