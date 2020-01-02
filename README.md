# marquez-airflow

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez-airflow/tree/master.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez-airflow/tree/master)
[![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/marquez-project/community)
[![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez-airflow/master/LICENSE)

A library that integrates Airflow DAGs with [Marquez](https://github.com/MarquezProject/marquez) for automatic metadata collection.

## Status

This library is under active development at [WeWork](https://www.wework.com).

## Requirements

 - [Python 3.5.0](https://www.python.org/downloads)+
 - [Airflow 1.10.3](https://pypi.org/project/apache-airflow)+

## Installation

```bash
$ pip install marquez-airflow
```

To install from source run:

```bash
$ python setup.py install
```
 
 ## Usage
 
 Once the library is installed in your system, your current DAGs need to be modified slightly by changing the import of `airflow.models.DAG` to `marquez.airflow.DAG`, see example below:
 
```python
from marquez_airflow import DAG
from airflow.operators.dummy_operator import DummyOperator


DAG_NAME = 'my_DAG_name'

default_args = {
    'marquez_location': 'github://data-dags/dag_location/',
    'marquez_input_urns': ["s3://some_data", "s3://more_data"],
    'marquez_output_urns': ["s3://output_data"],
    
    'owner': ...,
    'depends_on_past': False,
    'start_date': ...,
}

dag = DAG(DAG_NAME, schedule_interval='*/10 * * * *',
          default_args=default_args, description="yet another DAG")

run_this = DummyOperator(task_id='run_this', dag=dag)
run_this_too = DummyOperator(task_id='run_this_too', dag=dag)
run_this_too.set_upstream(run_this)
```

## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez-airflow/blob/master/CONTRIBUTING.md) for more details about how to contribute.
