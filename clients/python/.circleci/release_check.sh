#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
set -e -x -u

expected_version=$CIRCLE_TAG
current_version=$(python ./setup.py --version)
echo "current_version is ${current_version}"
echo "expected_version is ${expected_version}"

if [[ "${current_version}" != "${expected_version}" ]]; then
  echo "Packaging code ${current_version} does not equal to tagged version, ${expected_version}"
  exit 1
else
  exit 0
fi
