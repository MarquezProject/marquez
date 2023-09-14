### Using `get_changes`

The `get_changes.sh` script uses a fork of saadmk11/changelog-ci to get all
merged changes between two specified releases. To get all changes since the latest
release, set `END_RELEASE_VERSION` to the planned next release.

The changes will appear at the top of CHANGELOG.md.

#### Requirements

Python 3.10 or newer is required.
See the requirements.txt file for required dependencies.

The script also requires that the following environment variables be set:

`END_RELEASE_VERSION`\
`START_RELEASE_VERSION`\
`INPUT_GITHUB_TOKEN`

For example: `export END_RELEASE_VERSION=0.21.0`.

Use the planned next release for the end release version.

For instructions on creating a GitHub personal access token to use the GitHub API,
see: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token.

#### Running the script

Make the script executable (the first time), then run it with `./get_changes.sh`.
