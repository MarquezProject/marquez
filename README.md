[![CircleCI](https://circleci.com/gh/MarquezProject/marquez-python/tree/master.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez-python/tree/master) [![codecov](https://codecov.io/gh/MarquezProject/marquez-python/branch/master/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez-python/branch/master) [![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status) [![version](https://img.shields.io/pypi/v/marquez-python.svg)](https://pypi.python.org/pypi/marquez-python) [![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez-python/master/LICENSE) [![Known Vulnerabilities](https://snyk.io/test/github/MarquezProject/marquez-python/badge.svg)](https://snyk.io/test/github/MarquezProject/marquez-python)



# marquez-python

Marquez-Python is an open source client library package for python that users can use to help build applications that integrate with Marquez.

Marquez is an open source **metadata service** for the **collection**, **aggregation**, and **visualization** of a data ecosystem's metadata.
The marquez-python package is a library that interacts with a running instance of the Marquez server.


# Status
This project is under active development at WeWork. The latest published version can be found [here](https://pypi.org/project/marquez-python/). 

## Requirements.

Python 2.7 and 3.4+

## Installation & Usage
### pip install

If the python package is hosted on Github, you can install directly from Github

```sh
pip install git+https://github.com/GIT_USER_ID/GIT_REPO_ID.git
```
(you may need to run `pip` with root permission: `sudo pip install git+https://github.com/GIT_USER_ID/GIT_REPO_ID.git`)

Then import the package:
```python
import marquez_client 
```

### Setuptools

Install via [Setuptools](http://pypi.python.org/pypi/setuptools).

```sh
python setup.py install --user
```
(or `sudo python setup.py install` to install the package for all users)

Then import the package:
```python
import marquez_client
```

## Getting Started

Please follow the [installation procedure](#installation--usage) and then run the following:

Please be sure to set the environmental variables to connect with Marquez:
```
export MARQUEZ_HOST='localhost'
export MARQUEZ_PORT='8080'
```

## Documentation For Authorization

 All endpoints do not require authorization.


## Author




