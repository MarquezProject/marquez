There are two forms of release:
- Semantic
- Specific version

They are used to produce an auto-generated python API client that is versioned properly.
Running the script will:
- Update the version based on a specific value (specific version) or via semantic versioning (with the type - `major`, `minor`, or `patch`)
- Commit the diff
- Tag the commit
- Push the tags to master branch of the Marquez-Python-Codegen project

CircleCI will then release a PyPi artifact with that tagged version.

Example usage:
1. We want to release a Marquez-Python-Codegen client with version 1.2.0, perhaps because Marquez is now versioned to that value:
`./autogen.sh --set-version 1.2.0`

2. We want to do a patch increment of the Marquez-Python-Codegen client to incorporate some new, non-breaking functionality:
`./autogen.sh --semantic-bump patch`

3. We want to do a major increment of the Marquez-Python-Codegen client to incorporate a new breaking change:
`./autogen.sh --semantic-bump major`
