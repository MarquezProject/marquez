#!/bin/bash
set -e
set +x

export MARQUEZ_CLONE_DIR="/tmp/marquez"
export MARQUEZ_PYTHON_CLIENT_CODEGEN_CLONE_DIR="/tmp/marquez-python-client-codegen"
export OPEN_API_GENERATOR_CLONE_DIR="/tmp/openapi_generator"

get_current_package_version()
{
  echo $(python ./setup.py --version)
}

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

cat <<EOF | tee ${OPEN_API_GENERATOR_CLONE_DIR}/config.json
{
  "projectName": "marquez-python-codegen",
  "packageName": "marquez_codegen_client",
  "packageVersion": "${1}",
  "generateSourceCodeOnly" : "true"
}
EOF

}

get_latest_marquez_git_hash()
{
  cd ${MARQUEZ_CLONE_DIR}
  echo $(git rev-parse HEAD)
}

install_maven_if_necessary()
{
  set +e
  which mvn
  result=$?
  set -e
  if [[ "${result}" == 0 ]]; then
    echo "maven already installed"
    return 0
  else
    echo "installing maven"
    wget http://mirror.cc.columbia.edu/pub/software/apache/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz
    tar -xvf ./apache-maven-3.6.0-bin.tar.gz
    export PATH=$PATH:$(pwd)/apache-maven-3.6.0/bin
    echo "$PATH"
    echo "mvn installed at $(which mvn)"
  fi
}

regenerate_api_spec()
{
  cd ${MARQUEZ_PYTHON_CLIENT_CODEGEN_CLONE_DIR}  
  CURRENT_VERSION=$(get_current_package_version)
  
  generate_config_file ${CURRENT_VERSION}
  echo "Done creating config file"

  ls -la ${OPEN_API_GENERATOR_CLONE_DIR}/config.json

  docker run --rm -v /tmp:/tmp openapitools/openapi-generator-cli generate \
-i ${MARQUEZ_CLONE_DIR}/docs/openapi.yml \
-g python \
-o ${MARQUEZ_PYTHON_CLIENT_CODEGEN_CLONE_DIR} -c ${OPEN_API_GENERATOR_CLONE_DIR}/config.json \
 --skip-validate-spec

  marquez_latest_hash=$(get_latest_marquez_git_hash)

  cd ${MARQUEZ_PYTHON_CLIENT_CODEGEN_CLONE_DIR}
  git add -A
  git commit -a -m "Auto-updating marquez python codegen client based on Marquez commit ${marquez_latest_hash}"
}

refresh_codegen()
{
  install_maven_if_necessary
  clone_marquez_python_client_codegen
  clone_marquez
  echo "Regenerating API spec"
  regenerate_api_spec
  echo "Done regenerating spec" 
}

refresh_codegen

