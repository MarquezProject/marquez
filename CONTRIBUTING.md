# How to Contribute

We're excited you're interested in contributing to Marquez! We'd love your help, and there are plenty of ways to contribute:

* Fix or [report](https://github.com/MarquezProject/marquez/issues/new) a bug
* Fix or improve documentation
* Pick up a ["good first issue"](https://github.com/MarquezProject/marquez/labels/good%20first%20issue), then send a pull request our way

We feel that a welcoming community is important and we ask that you follow the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md) in all interactions with the community.

# Development

To run the entire test suite:

```bash
$ ./gradlew test
```

You can also run individual tests using the flag `--tests`:

```bash
$ ./gradlew test --tests marquez.api.resources.DatasetResourceTest
$ ./gradlew test --tests marquez.service.DatasetServiceTest
$ ./gradlew test --tests marquez.db.DatasetDaoTest
```

Or run tests by category:  

```bash
$ ./gradlew testUnit         # run only unit tests
$ ./gradlew testIntegration  # run only integration tests
$ ./gradlew testDataAccess   # run only data access tests
```

We use [spotless](https://github.com/diffplug/spotless) to format our code. This ensures `.java` files are formatted to comply with [Google Java Style](https://google.github.io/styleguide/javaguide.html). Make sure your code is formatted before pushing any changes, otherwise CI will fail:

```
$ ./gradlew spotlessApply
```

> **Note:** To make formatting code simple, we recommend installing a [plugin](https://github.com/google/google-java-format#intellij-android-studio-and-other-jetbrains-ides) for your favorite IDE. We also us [Lombok](https://projectlombok.org). Though not required, you might want to install the [plugin](https://projectlombok.org/setup/overview) as well. 

# Submitting a [Pull Request](https://help.github.com/articles/about-pull-requests)

1. [Fork](https://github.com/MarquezProject/marquez/fork) and clone the repository
2. Make sure all tests pass locally: `./gradlew test`
3. Create a new branch: `git checkout -b my-cool-new-branch`
4. Make change on your cool new branch
5. Write a test for your change
6. Make sure `.java` files are formatted: `./gradlew spotlessJavaCheck`
7. Push change to your fork and [submit a pull request](https://github.com/MarquezProject/marquez/compare)
8. Add the ["review"](https://github.com/MarquezProject/marquez/labels/review) label to your pull request
9. Work with project maintainers to get your change reviewed and merged into the `master` branch
10. Delete your branch

To ensure your pull request is accepted, follow these guidelines:

* All changes should be accompanied by tests
* Do your best to have a [well-formed commit message](https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html) for your change
* [Keep diffs small](https://graysonkoonce.com/stacked-pull-requests-keeping-github-diffs-small) and self-contained
* If your change fixes a bug, please [link the issue](https://help.github.com/articles/closing-issues-using-keywords) in your pull request description

# Resources

* [How to Contribute to Open Source](https://opensource.guide/how-to-contribute)
* [Using the Fork-and-Branch Git Workflow](https://blog.scottlowe.org/2015/01/27/using-fork-branch-git-workflow)
* [Keep a Changelog](https://keepachangelog.com)
