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
# Usage: $ ./wait-for-db.sh <host> <port> [timeout]

set -eu

host="${1}"
port="${2}"
timeInSecs="${3:-60s}"

if timeout ${timeInSecs} bash -c "./connect-to-db.sh ${host} ${port}"; then
    echo "Connection to db Successful"
else
    echo "Timeout, the postgres server is taking too long to respond."
fi