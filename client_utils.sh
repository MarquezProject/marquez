#!/bin/bash

export MARQUEZ_PYTHON_CLIENT_CLONE_DIR="/tmp/marquez-python-client-codegen"
export OPEN_API_GENERATOR_CLONE_DIR="/tmp/marquez-python-client-codegen"

get_current_package_version()
{
  echo $(python ./setup.py --version)
}

clone_marquez_python_client_codegen()
{
  clone_dir=${MARQUEZ_PYTHON_CLIENT_CLONE_DIR}
  rm -rf ${clone_dir} || true
  git clone git@github.com:MarquezProject/marquez-python-client-codegen.git ${clone_dir}
}

clone_openapi_generator()
{
  clone_dir=${OPEN_API_GENERATOR_CLONE_DIR}
  rm -rf ${clone_dir} || true

  git clone git@github.com:OpenAPITools/openapi-generator.git ${clone_dir}
}

regenerate_api_spec()
{
  cd ${MARQUEZ_PYTHON_CLIENT_CLONE_DIR}  
  CURRENT_VERSION=$(get_current_package_version)
  
  clone_openapi-generator
  cd ${OPEN_API_GENERATOR_CLONE_DIR}
  generate_config_file ${CURRENT_VERSION}
  mvn install -DskipTests
  
  cat ./config.json
  java -jar modules/openapi-generator-cli/target/openapi-generator-cli.jar generate \
   -i ~/git-projects/marquez/docs/openapi.yml \
   -g python \
   -o /tmp/${MARQUEZ_PYTHON_CLIENT_CLONE_DIR} -c ./config.json \
   --skip-validate-spec  
}




