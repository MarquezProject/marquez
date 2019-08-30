# Marquez Python Client

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez-python/tree/master.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez-python/tree/master) [![codecov](https://codecov.io/gh/MarquezProject/marquez-python/branch/master/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez-python/branch/master) [![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status) [![version](https://img.shields.io/pypi/v/marquez-python.svg)](https://pypi.python.org/pypi/marquez-python) [![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez-python/master/LICENSE)

Python client for [Marquez](https://github.com/MarquezProject/marquez).

## Status

This library is under active development at [The We Company](https://www.we.co). 

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

client = MarquezClient()

# create namespace
client.create_namespace('example-namespace', 'example-owner', 'example description')
```

To enable logging, set the environment variable `MARQUEZ_LOG_LEVEL` to `DEBUG`, `INFO`, or `ERROR`:

```
$ export MARQUEZ_LOG_LEVEL='INFO'
```
## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez-python/blob/master/CONTRIBUTING.md) for more details about how to contribute.
