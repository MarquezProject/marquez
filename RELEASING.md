These are the steps that will bump the release version, commit that code, and tag it. Once this is complete, the build system, CircleCI, will automatically build, test, and push the artifacts to PyPi under the `marquez-python` project located here: https://pypi.org/manage/project/marquez-python/releases/.

1. Make sure you are currently on the `main` branch.
2. Make sure to have the python package `bump2version` installed.
3. Run ./release.sh [major | minor | patch] to indicate whether the release will up the major, minor, or patch version respectively.
4. Verify that the correct tag is there.
5. Push the commit to the `main` branch.
6. Push to tag `git push origin <tag>`
7. Follow the CircleCI builds and Verify that they run successfully.
