# How to Contribute

We're excited you're interested in contributing to Marquez! We'd love your help, and there are plenty of ways to contribute:

* Fix or [report](https://github.com/MarquezProject/marquez-airflow/issues/new) a bug
* Fix or improve documentation
* Pick up a ["good first issue"](https://github.com/MarquezProject/marquez-airflow/labels/good%20first%20issue), then send a pull request our way

We feel that a welcoming community is important and we ask that you follow the [Contributor Covenant Code of Conduct](https://github.com/MarquezProject/marquez-airflow/blob/main/CODE_OF_CONDUCT.md) in all interactions with the community.

# Development
We recommend installing the following tools to run the tests:

Install pyenv: `brew install pyenv pyenv-virtualenv`
Add it to your profile: `~/.zshrc` or `~/.bashrc` depending on your shell
```
if command -v pyenv 1>/dev/null 2>&1; then
  eval "$(pyenv init -)"
  eval "$(pyenv virtualenv-init -)"
fi
```
create a marquez-airflow virtual env:
```
pyenv virtualenv marquez-airflow
pyenv activate marquez-airflow
```
(depending on the version of python you're running, you may need to alias pip to pip3)

Install the test dependencies:
```
pip3 install -r test-requirements.txt
```

Init the airflow local db:
```
airflow initdb
```

now you can run the tests:
`python -m pytest  --ignore=tests/integration tests/`

To run the linter locally:
install flake8
```
pip install flake8
```
and run it:
```
python -m flake8
```

# Submitting a [pull request](https://help.github.com/articles/about-pull-requests)

1. [Fork](https://github.com/MarquezProject/marquez-airflow/fork) and clone the repository
2. Make sure all tests pass locally: `python -m pytest --ignore=tests/integration`
3. Create a new branch: `git checkout -b my-cool-new-branch`
4. Make change on your cool new branch
5. Write a test for your change
6. Push change to your fork and [submit a pull request](https://github.com/MarquezProject/marquez-airflow/compare)
7. Add the ["review"](https://github.com/MarquezProject/marquez-airflow/labels/review) label to your pull request
8. Work with project maintainers to get your change reviewed and merged into the `main` branch
9. Delete your branch

To ensure your pull request is accepted, follow these guidelines:

* All changes should be accompanied by tests
* Do your best to have a [well-formed commit message](https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html) for your change
* [Keep diffs small](https://graysonkoonce.com/stacked-pull-requests-keeping-github-diffs-small) and self-contained
* If your change fixes a bug, please [link the issue](https://help.github.com/articles/closing-issues-using-keywords) in your pull request description

# Resources

* [How to Contribute to Open Source](https://opensource.guide/how-to-contribute)
* [Using the Fork-and-Branch Git Workflow](https://blog.scottlowe.org/2015/01/27/using-fork-branch-git-workflow)
* [Keep a Changelog](https://keepachangelog.com)
