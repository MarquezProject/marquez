# marquez-airflow

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez-airflow/tree/master.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez-airflow/tree/master)
[![codecov](https://codecov.io/gh/MarquezProject/marquez-airflow/branch/master/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez-airflow/branch/master)
[![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/marquez-project/community)
[![version](https://img.shields.io/pypi/v/marquez-airflow.svg)](https://pypi.python.org/pypi/marquez-airflow)
[![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez-airflow/master/LICENSE)

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

To install from source run:

```bash
$ python3 setup.py install
```
 
 ## Usage
 
To use this library, the line `from airflow import DAG` needs to be replaced by `from marquez_airflow import DAG`, see example below:
 
```python
from datetime import datetime
from marquez_airflow import DAG
from airflow.operators.postgres_operator import PostgresOperator
from airflow.utils.dates import days_ago

default_args = {
    'owner': 'datascience',
    'depends_on_past': False,
    'start_date': days_ago(7),
    'email_on_failure': False,
    'email_on_retry': False,
    'email': ['datascience@datakin.com']
}

dag = DAG(
    'etl_orders_7_days',
    schedule_interval='@weekly',
    default_args=default_args,
    description='Loads newly placed orders weekly.'
)

t1 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS orders_7_days (
      order_id      INTEGER REFERENCES orders(id),
      placed_on     TIMESTAMP NOT NULL,
      discount_id   INTEGER REFERENCES discounts(id),
      menu_id       INTEGER REFERENCES menus(id),
      restaurant_id INTEGER REFERENCES restaurants(id),
      menu_item_id  INTEGER REFERENCES menu_items(id),
      category_id   INTEGER REFERENCES categories(id)
    );''',
    dag=dag
)

t2 = PostgresOperator(
    task_id='tuncate',
    postgres_conn_id='food_delivery_db',
    sql='TRUNCATE TABLE orders_7_days;',
    dag=dag
)

t3 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO orders_7_days (order_id, placed_on, discount_id, menu_id, restaurant_id, menu_item_id, category_id)
      SELECT o.id AS order_id, o.placed_on, o.discount_id, m.id AS menu_id, m.restaurant_id, mi.id AS menu_item_id, c.id AS category_id
        FROM orders AS o
       INNER JOIN menu_items AS mi
          ON mi.id = o.menu_item_id
       INNER JOIN categories AS c
          ON c.id = mi.category_id
       INNER JOIN menus AS m
          ON m.id = c.menu_id
       WHERE o.placed_on >= NOW() - interval '7 days'
    ''',
    dag=dag
)

t1 >> t2 >> t3
```

## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez-airflow/blob/master/CONTRIBUTING.md) for more details about how to contribute.
