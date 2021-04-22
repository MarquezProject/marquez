#!/bin/bash

for l in `ls docs/assets/dot`; do
  docker run --rm -v ${PWD}/docs/assets/dot:/graphs -i nshine/dot dot -Tpng -O /graphs/$l
done