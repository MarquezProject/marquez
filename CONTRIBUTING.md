# How to Contribute

We're excited you're interested in contributing to Marquez! We'd love your help, and there are plenty of ways to contribute:

* Give the repo a star
* Join our [slack](http://bit.ly/MarquezSlack) channel and leave us feedback or help with answering questions from the community
* Fix or [report](https://github.com/MarquezProject/marquez/issues/new) a bug
* Fix or improve documentation
* For newcomers, pick up a ["good first issue"](https://github.com/MarquezProject/marquez/labels/good%20first%20issue), then send a pull request our way (see the [resources](#resources) section below for helpful links to get started)

We feel that a welcoming community is important and we ask that you follow the [Contributor Covenant Code of Conduct](https://github.com/MarquezProject/marquez/blob/main/CODE_OF_CONDUCT.md) in all interactions with the community.

If you’re interested in using or learning more about Marquez, reach out to us on our [slack](http://bit.ly/MarquezSlack) channel and follow [@MarquezProject](https://twitter.com/MarquezProject) for updates. We also encourage new comers to [join](https://lists.lfaidata.foundation/g/marquez-technical-discuss/ics/invite.ics?repeatid=32038) our monthly community meeting!

# Getting Your Changes Approved

Your pull request must be approved and merged by a [committer](COMMITTERS.md).

# Development

To run the entire test suite:

```bash
$ ./gradlew test
```

You can also run individual tests for a [submodule](https://github.com/MarquezProject/marquez#modules) using the `--tests` flag:

```bash
$ ./gradlew :api:test --tests marquez.api.OpenLineageResourceTest
$ ./gradlew :api:test --tests marquez.service.OpenLineageServiceIntegrationTest
$ ./gradlew :api:test --tests marquez.db.OpenLineageDaoTest
```

Or run tests by category:

```bash
$ ./gradlew :api:testUnit         # run only unit tests
$ ./gradlew :api:testIntegration  # run only integration tests
$ ./gradlew :api:testDataAccess   # run only data access tests
```

We use [spotless](https://github.com/diffplug/spotless) to format our code. This ensures `.java` files are formatted to comply with [Google Java Style](https://google.github.io/styleguide/javaguide.html). Make sure your code is formatted before pushing any changes, otherwise CI will fail:

```
$ ./gradlew spotlessApply
```

> **Note:** To make formatting code simple, we recommend installing a [plugin](https://github.com/google/google-java-format#intellij-android-studio-and-other-jetbrains-ides) for your favorite IDE. We also us [Lombok](https://projectlombok.org). Though not required, you might want to install the [plugin](https://projectlombok.org/setup/overview) as well.

# `.git/hooks`

We use [`pre-commit`](https://pre-commit.com/index.html) to manage git hooks:

```bash
$ brew install pre-commit
```

To setup the git hook scripts run:

```
$ pre-commit install
```

# `.github/workflows`

Each Pull Request executes a series of quality checks, mostly relying upon CircleCI for validation. However, there are
certain validation checks that execute via GitHub Actions and can be run locally using the steps below.

Install [act](https://github.com/nektos/act) and run the following command, which will evaluate the GitHub Actions
checks that apply to each Pull Request. The first time you run _act_ you will be asked to choose a
[runner](https://github.com/nektos/act#runners).

Alternatively, you can store your preferred runner within a local user profile named _.actrc_.

```bash
# .actrc file example (https://github.com/nektos/act#configuration)
-P ubuntu-latest=ghcr.io/catthehacker/ubuntu:act-latest
```

Once you have configured a runner, use _act_ to invoke GitHub Actions and evaluate the workflow.

```bash
act pull_request
```

You can also enable verbose logging and image caching via [act flags](https://github.com/nektos/act#flags).

```bash
act pull_request --reuse --verbose
```

> **Note:** Docker must be running in order to utilize _act_.

# Troubleshooting

There is an issue within the _act_ tool that prevents the _kind_ cluster from being deleted after execution the action.
When this condition exists, you will experience the error below.

```bash
| Creating kind cluster...
| ERROR: failed to create cluster: node(s) already exist for a cluster with the name "chart-testing"
[Lint and Test Chart/lint-test]   ❌  Failure - Create kind cluster
```

Execute the command below to manually clean up the _kind_ cluster and resolve the problem.

```bash
kind delete clusters chart-testing
```

# Publish to Local Maven Repository

Use [`publishToMavenLocal`](https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:tasks) to publish artifacts to your local maven repository:

```
$ ./gradlew publishToMavenLocal
```

# Submitting a [Pull Request](https://help.github.com/articles/about-pull-requests)

1. [Fork](https://github.com/MarquezProject/marquez/fork) and clone the repository
2. Make sure all tests pass locally: `./gradlew :api:test`
3. Create a new [branch](#branching): `git checkout -b feature/my-cool-new-feature`
4. Make change on your cool new branch
5. Write a test for your change
6. Make sure `.java` files are formatted: `./gradlew spotlessJavaCheck`
7. Make sure `.java` files contain a [copyright and license header](#copyright--license)
8. Make sure to [sign you work](#sign-your-work)
9. Push change to your fork and [submit a pull request](https://github.com/MarquezProject/marquez/compare)
10. Work with project maintainers to get your change reviewed and merged into the `main` branch
11. Delete your branch

To ensure your pull request is accepted, follow these guidelines:

* All changes should be accompanied by tests
* Do your best to have a [well-formed commit message](https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html) for your change
* [Keep diffs small](https://kurtisnusbaum.medium.com/stacked-diffs-keeping-phabricator-diffs-small-d9964f4dcfa6) and self-contained
* If your change fixes a bug, please [link the issue](https://help.github.com/articles/closing-issues-using-keywords) in your pull request description
* Any changes to the API reference requires [regenerating](#api-docs) the static `openapi.html` file.

> **Note:** A pull request should generally contain only one commit (use `git commit --amend` and `git push --force` or [squash](http://gitready.com/advanced/2009/02/10/squashing-commits-with-rebase.html) existing commits into one).

# Branching

* Use a _group_ at the beginning of your branch names

  ```
  feature  Add or expand a feature
  bug      Fix a bug
  proposal Propose a change
  ```

  _For example_:

  ```
  feature/my-cool-new-feature
  bug/my-bug-fix
  bug/my-other-bug-fix
  proposal/my-proposal
  ```

* Choose _short_ and _descriptive_ branch names
* Use dashes (`-`) to separate _words_ in branch names
* Use _lowercase_ in branch names

# Dependencies

We use [renovate](https://github.com/renovatebot/renovate) to manage dependencies for most of our project modules,
with a couple of exceptions. Renovate automatically detects new dependency versions, and opens pull
requests to upgrade dependencies in accordance to the [configured rules](https://github.com/MarquezProject/marquez/blob/main/renovate.json).

The following dependencies are managed manually

* _Web code_ - it is challenging to programmatically validate web content
* _Spark versions_ - the internal query plans parsed by the Spark OpenLineage integration are not stable across Spark versions
* _Gradle_ - this tool orchestrates the entire build pipeline and was excluded to ensure stability

# Sign Your Work

The _sign-off_ is a simple line at the end of the message for a commit. All commits needs to be signed. Your signature certifies that you wrote the patch or otherwise have the right to contribute the material (see [Developer Certificate of Origin](https://developercertificate.org)):

```
This is my commit message

Signed-off-by: Remedios Moscote <remedios.moscote@buendía.com>
```

Git has a [`-s`](https://git-scm.com/docs/git-commit#Documentation/git-commit.txt---signoff) command line option to append this automatically to your commit message:

```bash
$ git commit -s -m "This is my commit message"
```

# API [Docs](https://github.com/MarquezProject/marquez/tree/main/docs)

To bundle:

```bash
$ redoc-cli bundle spec/openapi.yml -o docs/openapi.html  --title "Marquez API Reference"
```

To serve:

```bash
$ redoc-cli serve spec/openapi.yml
```

Then browse to: http://localhost:8080

> **Note:** To bundle or serve the API docs, please install [redoc-cli](https://www.npmjs.com/package/redoc-cli).

# `COPYRIGHT` / `LICENSE`

We use [SPDX](https://spdx.dev) for copyright and license information. The following license header **must** be included in all `java,` `bash`, and `py` source files:

`java`

```
/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */
```

`bash`

```
#!/bin/bash
#
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
```

`py`

```
# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0
```

# Resources

* [How to Contribute to Open Source](https://opensource.guide/how-to-contribute)
* [Using the Fork-and-Branch Git Workflow](https://blog.scottlowe.org/2015/01/27/using-fork-branch-git-workflow)
* [Understanding the GitHub flow](https://guides.github.com/introduction/flow/)
* [Keep a Changelog](https://keepachangelog.com)
* [Code Review Developer Guide](https://google.github.io/eng-practices/review)
* [Signing Commits](https://docs.github.com/en/github/authenticating-to-github/signing-commits)
