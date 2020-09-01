# Marquez Python Client

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez-python/tree/main.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez-python/tree/main)
[![codecov](https://codecov.io/gh/MarquezProject/marquez-python/branch/main/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez-python/branch/main)
[![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/marquez-project/community)
[![version](https://img.shields.io/pypi/v/marquez-python.svg)](https://pypi.python.org/pypi/marquez-python)
[![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez-python/main/LICENSE)

Python client for [Marquez](https://github.com/MarquezProject/marquez).

## Status

This library is under active development at [Datakin](http://datak.in/). 

## Documentation

See the [API docs](https://marquezproject.github.io/marquez/openapi.html).

## Requirements

[Python 3.5.0](https://www.python.org/downloads/)+

## Installation

```bash
$ pip3 install marquez-python
```

To install from source run:

```bash
$ python3 setup.py install
```

## Usage

```python
from marquez_client import MarquezClient

client = MarquezClient(url='http;//localhost:5000')

# create namespace
client.create_namespace('example-namespace', 'example-owner', 'example description')
```

To enable logging, set the environment variable `MARQUEZ_LOG_LEVEL` to `DEBUG`, `INFO`, or `ERROR`:

```
$ export MARQUEZ_LOG_LEVEL='INFO'
```
## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez-python/blob/main/CONTRIBUTING.md) for more details about how to contribute.
