# How to Contribute

We're excited you're interested in contributing to Marquez! We'd love your help, and there are plenty of ways to contribute:

* Fix or [report](https://github.com/MarquezProject/marquez-web/issues/new) a bug
* Fix or improve documentation
* Pick up a ["good first issue"](https://github.com/MarquezProject/marquez-web/labels/good%20first%20issue), then send a pull request our way

We feel that a welcoming community is important and we ask that you follow the [Contributor Covenant Code of Conduct](https://github.com/MarquezProject/marquez-web/blob/master/CODE_OF_CONDUCT.md) in all interactions with the community.

# Submitting a [Pull Request](https://help.github.com/articles/about-pull-requests)

1. [Fork](https://github.com/MarquezProject/marquez-web/fork) and clone the repository
3. Create a new [branch](#branching): `git checkout -b feature/my-cool-new-feature`
3. Make change on your cool new branch
4. Write a test for your change
5. Push change to your fork and [submit a pull request](https://github.com/MarquezProject/marquez-web/compare)
6. Add the ["review"](https://github.com/MarquezProject/marquez-web/labels/review) label to your pull request
7. Work with project maintainers to get your change reviewed and merged into the `master` branch
8. Delete your branch

To ensure your pull request is accepted, follow these guidelines:

* All changes should be accompanied by tests
* Do your best to have a [well-formed commit message](https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html) for your change
* [Keep diffs small](https://graysonkoonce.com/stacked-pull-requests-keeping-github-diffs-small) and self-contained
* If your change fixes a bug, please [link the issue](https://help.github.com/articles/closing-issues-using-keywords) in your pull request description

> **Note:** A pull request should generally contain only one commit (use `git commit --amend` and `git --force push` or [squash](http://gitready.com/advanced/2009/02/10/squashing-commits-with-rebase.html) existing commits into one).

# Branching

* Use a _group_ at the beginning of your branch names

  ```
  wip      Work on a feature is still in progress
  feature  Add or expand a feature
  bug      Fix a bug
  ```
  
  _For example_:
  
  ```
  wip/my-cool-new-wip-feature
  feature/my-cool-new-feature
  feature/my-other-cool-new-feature
  bug/my-bug-fix
  bug/my-other-bug-fix
  ```
  
* Choose _short_ and _descriptive_ branch names
* Use dashes (`-`) to separate _words_ in branch names
* Use _lowercase_ in branch names

# Resources

* [How to Contribute to Open Source](https://opensource.guide/how-to-contribute)
* [Using the Fork-and-Branch Git Workflow](https://blog.scottlowe.org/2015/01/27/using-fork-branch-git-workflow)
* [Keep a Changelog](https://keepachangelog.com)
* [Code Review Developer Guide](https://google.github.io/eng-practices/review)
