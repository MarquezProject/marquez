# Marquez Airflow - `DEPRECATED`

[![Project status](https://img.shields.io/badge/status-deprecated-orange.svg)]()

This repository has been moved to [`OpenLineage/integration/airflow`](https://github.com/OpenLineage/OpenLineage/tree/main/integration/airflow). **The** `marquez-airflow` **library extends the class** [`openlineage.airflow.DAG`](https://github.com/OpenLineage/OpenLineage/blob/main/integration/airflow/openlineage/airflow/dag.py) **for backwards compatibility.**

## Requirements

[Python 3.5.0](https://www.python.org/downloads)+

## Installation

```bash
$ pip3 install marquez-airflow
```

To install from source run:

```bash
$ python3 setup.py install
```

## Configuration

Configure the following environment variables in your Airflow instance:

```bash
MARQUEZ_URL=<marquez-url>              # The URL of the HTTP backend
MARQUEZ_NAMESPACE=<marquez-namespace>  # The namespace associated with the collected metadata
```

## Usage

To begin collecting DAG lineage metadata with Marquez, make the following change to your DAG definitions:

```diff
- from airflow import DAG
+ from marquez_airflow import DAG
```