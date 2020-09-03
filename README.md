# marquez-airflow

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez-airflow/tree/main.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez-airflow/tree/main)
[![codecov](https://codecov.io/gh/MarquezProject/marquez-airflow/branch/main/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez-airflow/branch/main)
[![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/marquez-project/community)
[![version](https://img.shields.io/pypi/v/marquez-airflow.svg)](https://pypi.python.org/pypi/marquez-airflow)
[![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez-airflow/main/LICENSE)

A library that integrates [Airflow `DAGs`]() with [Marquez](https://github.com/MarquezProject/marquez) for automatic metadata collection.

## Status

This library is under active development at [Datakin](https://twitter.com/DatakinHQ). 

## Requirements

 - [Python 3.5.0](https://www.python.org/downloads)+
 - [Airflow 1.10.3](https://pypi.org/project/apache-airflow)+

## Installation

```bash
$ pip3 install marquez-airflow
```

To install from source, run:

```bash
$ python3 setup.py install
```
## Settings

### Pointing to your Marquez service
`marquez-airflow` needs to know where to talk to the Marquez server API.  You can set these using environment variables to be read by your Airflow service.

You will also need to set the namespace if you are using something other than the `default` namespace.
```
MARQUEZ_URL=http://my_hosted_marquez.example.com:5000
MARQUEZ_NAMESPACE=my_special_ns
```

*NOTE: In the latest version of `marquez-python`, the constructor requires a `url` parameter for the host and port.  Presumably, the logic to read the env vars will move into this library*

### Extractors : Sending the correct data from your DAGs
If you do nothing, Marquez will receive the `Job` and the `Run` from your DAGs, but sources and datasets will not be sent.

`marquez-airflow` allows you to do more than that by building "Extractors".  Extractors are in the process of changing right now, but they basically take a task and extract:
1. Name : The name of the task
1. Location : Location of the code for the task
1. Inputs : List of input datasets
1. Outputs : List of output datasets
1. Context : The Airflow context for the task

It's important to understand the inputs and outputs are lists and relate directly to the `Dataset` object in Marquez.  Datasets also include a source which relates directly to the `Source` object in Marquez.

*A PostgresExtractor is currently in progress.  When that's merged, it will represent a good example of how to write custom extractors*

## Usage

```python
from datetime import datetime
from marquez_airflow import DAG
from airflow.operators.postgres_operator import PostgresOperator
from airflow.utils.dates import days_ago

default_args = {
    'owner': 'datascience',
    'depends_on_past': False,
    'start_date': days_ago(1),
    'email_on_failure': False,
    'email_on_retry': False,
    'email': ['datascience@datakin.com']
}

dag = DAG(
    'orders_popular_day_of_week',
    schedule_interval='@weekly',
    default_args=default_args,
    description='Determines the popular day of week orders are placed.'
)

t1 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS popular_orders_day_of_week (
      order_day_of_week VARCHAR(64) NOT NULL,
      order_placed_on   TIMESTAMP NOT NULL,
      orders_placed     INTEGER NOT NULL
    );''',
    dag=dag
)

t2 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO popular_orders_day_of_week (order_day_of_week, order_placed_on, orders_placed)
      SELECT EXTRACT(ISODOW FROM order_placed_on) AS order_day_of_week,
             order_placed_on,
             COUNT(*) AS orders_placed
        FROM top_delivery_times
       GROUP BY order_placed_on;
    ''',
    dag=dag
)

t1 >> t2
```

## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez-airflow/blob/main/CONTRIBUTING.md) for more details about how to contribute.
