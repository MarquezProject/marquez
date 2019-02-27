#!/bin/bash
#
# A script used to deploy Marquez
#
# Usage: $ ./deploy.sh [OPTIONS] <version>

readonly VERSION_REGEX="^[0-9]+(\\.[0-9]+){2}\\.([0-9]){4}([0-9]){2}([0-9]){2}\\.([0-9a-f]){7}$" # X.Y.Z.YYYMMDD.SHA-1
readonly WEK8S_PHOENIX="wek8s-phoenix"
readonly WEK8S_KRAKEN="wek8s-kraken"
readonly MARQUEZ_HOST_PHOENIX="marquez.phoenix.dev.wwrk.co"
readonly MARQUEZ_HOST_KRAKEN="marquez.kraken.wwrk.co"
readonly DEFAULT_TIMEOUT=300

usage() {
cat << EOF
Marquez deployment script

Usage: ./$(basename "${0}") [OPTIONS] <version>

OPTIONS:
  -p         deploy to production (default: staging)
  -t int     the duration (in seconds) to wait (default: 300)
  -v string  version (format: X.Y.Z.YYYMMDD.SHA-1)
  -h         show this help message
EOF
}

# Change working directory to project root
project_root=$(git rev-parse --show-toplevel)
cd "${project_root}"

env="${WEK8S_PHOENIX}"
host="${MARQUEZ_HOST_PHOENIX}"
timeout="${DEFAULT_TIMEOUT}"
version=""

while getopts ":pht:v:" opt; do
  case "${opt}" in
    p)
      env="${WEK8S_KRAKEN}"
      host="${MARQUEZ_HOST_KRAKEN}"
      ;;
    v)
      version="${OPTARG}"
      ;;
    h)
      usage
      exit 0
      ;;
    \?)
      echo "Invalid option: -${OPTARG}"
      usage
      exit 1
      ;;
    :)
      echo "Invalid option: -${OPTARG} requires an argument"
      usage
      exit 1
      ;;
  esac
done
shift $((OPTIND -1))

# Ensure valid version
if [[ ! "${version}" =~ ${VERSION_REGEX} ]]; then
  echo "Version must match ${VERSION_REGEX}"
  exit 1
fi

echo "export INGRESS_HOST=${host}" >> $BASH_ENV
source "${BASH_ENV}"

/usr/bin/deploy_helper fetch_reqs -e "${env}" -n dataplatform || exit 1

helmfile --file .deploy/helmfile.yml apply  \
  --args "--set image.tag=${version}" || exit 1

echo "DONE!"
