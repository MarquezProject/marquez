#!/bin/bash
function does_package_version_match()
{
  expected_version=$(git describe --tags | head -n 1)
  current_version=$(python ./setup.py --version)
  echo "current_version is ${current_version}"
  echo "expected_version is ${expected_version}"

  if [[ "${current_version}" != "${expected_version}" ]]; then

    echo "no match"
    return 1
  else

    echo "match"
    return 0
  fi
}

does_package_version_match
