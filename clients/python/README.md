# Marquez Python Client

Python client for [Marquez](https://github.com/MarquezProject/marquez).

## Documentation

See the [API docs](https://marquezproject.github.io/marquez/openapi.html).

## Requirements

* [Python 3.5.0](https://www.python.org/downloads)+

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
