#!/bin/bash
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Usage: $ ./replace-gradle-version.sh

set -e

NEW_VERSION="$1"
shift

for FILE in "$@"
do
  sed -i 's/\(^version=.*$\)/version='"${NEW_VERSION}"'/g' "$FILE"
done