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
# Usage: $ ./get-jdk17.sh

set -e

wget -qO - https://adoptium.jfrog.io/adoptium/api/gpg/key/public | sudo apt-key add -
sudo add-apt-repository --yes https://adoptium.jfrog.io/adoptium/deb
sudo apt-get update && sudo apt-get install temurin-17-jdk
sudo update-alternatives --set java /usr/lib/jvm/temurin-17-jdk-amd64/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/temurin-17-jdk-amd64/bin/javac
java -version

echo "DONE!"
