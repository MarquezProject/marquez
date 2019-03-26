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
set -x

export MARQUEZ_CLONE_DIR="/tmp/marquez"
export MARQUEZ_PYTHON_CODEGEN_CLONE_DIR="/tmp/marquez-python-codegen"
export OPEN_API_GENERATOR_CLONE_DIR="/tmp/openapi_generator"
export CONFIG_FILE_LOCATION="${MARQUEZ_PYTHON_CODEGEN_CLONE_DIR}/config.json"

clone_marquez_python_client_codegen()
{
  clone_dir=${MARQUEZ_PYTHON_CODEGEN_CLONE_DIR}
  rm -rf ${clone_dir} || true
  git clone git@github.com:MarquezProject/marquez-python-codegen.git ${clone_dir}
}

clone_marquez()
{
  clone_dir=${MARQUEZ_CLONE_DIR}
  rm -rf ${clone_dir} || true

  git clone --depth=1 https://github.com/MarquezProject/marquez.git ${clone_dir}
}

update_config_file()
{
  sed -i '' "s/packageVersion.*/packageVersion\": \"${version}\",/g" ${CONFIG_FILE_LOCATION}
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
  -o ${MARQUEZ_PYTHON_CODEGEN_CLONE_DIR} -c ${CONFIG_FILE_LOCATION} \
  --skip-validate-spec
}

commit_changes()
{
  marquez_latest_hash=$(get_latest_marquez_git_hash)

  cd ${MARQUEZ_PYTHON_CODEGEN_CLONE_DIR}
  git add -A
  git commit -a -m "Auto-updating marquez python codegen client based on Marquez commit ${marquez_latest_hash}"
}

setup_repos()
{
  clone_marquez_python_client_codegen
  clone_marquez
}

verify_bumpversion_installed() {
  set +e
  which bumpversion
  result=$?
  set -e
  if [[ "result" != "0" ]]; then
    echo "Please install bumpversion in order to use this script"
    exit 1
  fi
  echo "Dependency check for bumpversion passed."
}

refresh_codegen()
{
  # The {new_version} is not a bash variable - it's a bumpversion notation for the new version.
  # Please see bumpversion --help for more information.
  cd ${MARQUEZ_PYTHON_CODEGEN_CLONE_DIR}
  current_version=$(python ${MARQUEZ_PYTHON_CODEGEN_CLONE_DIR}/setup.py --version)
  if [ -n "${new_version}" ]; then
    echo "Upping the version to be ${new_version}"
    # Note: the type here, patch, does not matter as we are just upgrading
    # to a specified version. It's required to satisfy the arg parsing.
    bumpversion --current-version ${current_version} --new-version ${new_version} --commit patch ./setup.py
  else
    bumpversion --current-version ${current_version} --commit ${type} ./setup.py
  fi

  version=$(python ${MARQUEZ_PYTHON_CODEGEN_CLONE_DIR}/setup.py --version)

  update_config_file
  regenerate_api_spec
  commit_changes

  cd ${MARQUEZ_PYTHON_CODEGEN_CLONE_DIR}
  git tag ${version}
  git push --tags origin master
}
