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
# Usage: $ source ./common_packaging_utils.sh

set -e
set +x

export MARQUEZ_CLONE_DIR="/tmp/marquez"
export MARQUEZ_PYTHON_CLIENT_CODEGEN_CLONE_DIR="/tmp/marquez-python-client-codegen"
export OPEN_API_GENERATOR_CLONE_DIR="/tmp/openapi_generator"

get_current_marquez_python_client_codegen_version()
{
  echo $(python ${MARQUEZ_PYTHON_CLIENT_CLONE_DIR}/setup.py --version)
}

clone_marquez_python_client_codegen()
{
  clone_dir=${MARQUEZ_PYTHON_CLIENT_CODEGEN_CLONE_DIR}
  rm -rf ${clone_dir} || true
  git clone https://github.com/ashulmanwework/marquez-python-client-codegen.git ${clone_dir}
}

clone_marquez()
{
  clone_dir=${MARQUEZ_CLONE_DIR}
  rm -rf ${clone_dir} || true

  git clone --depth=1 https://github.com/MarquezProject/marquez.git ${clone_dir}
}

clone_openapi_generator()
{
  clone_dir=${OPEN_API_GENERATOR_CLONE_DIR}
  rm -rf ${clone_dir} || true

  echo "cloning API generator into ${OPEN_API_GENERATOR_CLONE_DIR}"
  git clone --depth=1 https://github.com/OpenAPITools/openapi-generator.git ${clone_dir}
}

generate_config_file()
{
rm -f /tmp/config.json
version=$(python ${MARQUEZ_PYTHON_CLIENT_CLONE_DIR}/setup.py --version)
cat <<EOF | tee /tmp/config.json
{
  "projectName": "marquez-python-codegen",
  "packageName": "marquez_codegen_client",
  "packageVersion": "${version}",
  "generateSourceCodeOnly" : "true"
}
EOF

}

get_latest_marquez_git_hash()
{
  cd ${MARQUEZ_CLONE_DIR}
  echo $(git rev-parse HEAD)
}

regenerate_api_spec()
{
  docker run --rm -v /tmp:/tmp openapitools/openapi-generator-cli generate \
-i ${MARQUEZ_CLONE_DIR}/docs/openapi.yml \
-g python \
-o ${MARQUEZ_PYTHON_CLIENT_CODEGEN_CLONE_DIR} -c /tmp/config.json \
 --skip-validate-spec
}

tag_commit()
{
  marquez_latest_hash=$(get_latest_marquez_git_hash)

  cd ${MARQUEZ_PYTHON_CLIENT_CODEGEN_CLONE_DIR}
  git add -A
  git commit -a -m "Auto-updating marquez python codegen client based on Marquez commit ${marquez_latest_hash}"
}

setup_repos()
{
  clone_marquez_python_client_codegen
  clone_marquez
  generate_config_file
}

refresh_codegen()
{
  setup_repos
  regenerate_api_spec
  tag_commit
}

