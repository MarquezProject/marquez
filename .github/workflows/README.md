# Github Super-Linter Workflows

## Purpose

While technically linters, these workflows check newly added source files in the project for properly formatted license and copyright lines. This method is employed instead of CircleCI workflows because Super-Linter is more easily configurable, with little code, and offers linters for all languages in the project that are pluggable and configurable from a single yaml file.

## Linters

- Java: checkstyles

## Configuration

Linter workflows are serialized and configured in `linter.yml` using environment variables.

For a list of the variables required for each linter, see the Github Actions [docs](https://github.com/marketplace/actions/super-linter#how-to-use).

