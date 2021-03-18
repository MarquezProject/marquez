# [Airflow](https://airflow.apache.org) Example

In this example, we'll walk you through how to enable an **Airflow DAG** to send lineage metadata to **Marquez**. The example will help demonstrate some of the features of Marquez.

### What you’ll learn:

* Enable Marquez in Airflow
* Write your very first Marquez enabled DAG
* Troubleshoot a failing DAG using Marquez

## Prerequisites

Before you begin, make sure you have installed:

* [Docker 17.05](https://docs.docker.com/install)+
* [Docker Compose](https://docs.docker.com/compose/install)

> **Note:** We recommend that you have allocated at least **2 CPUs** and **8 GB** of memory to Docker.

## Step 1: Enable Marquez in Airflow

* To download and install the latest [`marquez-airflow`](https://pypi.org/project/marquez-airflow) when starting Airflow, you'll need to create a `requirements.txt` file with the following content:

  ```
  marquez-airflow
  ```

* Next, we'll need to specify where to send DAG metadata. Create a config file named `marquez.env` with the following environment variables:

  ```
  MARQUEZ_BACKEND=http             # Collect metadata using HTTP backend
  MARQUEZ_URL=http://marquez:5000  # The URL of the HTTP backend
  MARQUEZ_NAMESPACE=example        # The namespace associated with the collected metadata
  ```
  > **Note:** The `marquez.env` config file will be used by the `airflow`, `airflow_scheduler`, and `airflow_worker` containers to send lineage metadata to Marquez.
  
* Your `examples/airflow/` directory should now contain the following:

  ```
  .
  ├── docker/
  ├── docs/
  ├── marquez.env
  └── requirements.txt
  ```

## Step 2: Start Airflow with Marquez

* Create the `dags/` folder where our example DAGs will be located:

  ```bash
  $ mkdir dags
  ```

* Then, start Airflow:

  ```bash
  $ docker-compose up
  ```

  > **Tip:** Use `-d` to run in detached mode.

  **The above command will:**

  * Start Airflow and install `marquez-airflow` used to collect DAG metadata
  * Start Marquez
  * Start Postgres

To view the Airflow UI and verify it's running, open http://localhost:8080. You can also browse to http://localhost:3000 to view the Marquez UI.

## Step 3: Write Airflow DAGs using Marquez

Now that we've started Airflow and Marquez, we can add the dags `counter.py` and `sum.py` (defined below) to our `dags/` folder:

### DAG `counter.py`

In the `dags/` folder, create a file named `counter.py` and copy in the following code:

```python
import random

from marquez_airflow import DAG
from airflow.operators.postgres_operator import PostgresOperator
from airflow.utils.dates import days_ago

default_args = {
    'owner': 'datascience',
    'depends_on_past': False,
    'start_date': days_ago(1),
    'email_on_failure': False,
    'email_on_retry': False,
    'email': ['datascience@example.com']
}

dag = DAG(
    'counter',
    schedule_interval='*/1 * * * *',
    catchup=False,
    is_paused_upon_creation=False,
    default_args=default_args,
    description='DAG that generates a new count value between 1-10.'
)

t1 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='example_db',
    sql='''
    CREATE TABLE IF NOT EXISTS counts (
      value INTEGER
    );''',
    dag=dag
)

t2 = PostgresOperator(
    task_id='inc',
    postgres_conn_id='example_db',
    sql='''
    INSERT INTO counts (value)
         VALUES (%(value)s)
    ''',
    parameters={
      'value': random.randint(1, 10)
    },
    dag=dag
)

t1 >> t2

```

### DAG `sum.py`

In the `dags/` folder, create a file named `sum.py` and copy in the following code:

```python
from marquez_airflow import DAG
from airflow.operators.postgres_operator import PostgresOperator
from airflow.utils.dates import days_ago

default_args = {
    'owner': 'datascience',
    'depends_on_past': False,
    'start_date': days_ago(1),
    'email_on_failure': False,
    'email_on_retry': False,
    'email': ['datascience@example.com']
}

dag = DAG(
    'sum',
    schedule_interval='*/5 * * * *',
    catchup=False,
    is_paused_upon_creation=False,
    default_args=default_args,
    description='DAG that sums the total of generated count values.'
)

t1 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='example_db',
    sql='''
    CREATE TABLE IF NOT EXISTS sums (
      value INTEGER
    );''',
    dag=dag
)

t2 = PostgresOperator(
    task_id='total',
    postgres_conn_id='example_db',
    sql='''
    INSERT INTO sums (value)
        SELECT SUM(c.value) FROM counts AS c;
    ''',
    dag=dag
)

t1 >> t2

```

## Step 4: Troubleshoot Failing DAG with Marquez


## Running on [GCP](https://cloud.google.com/composer)

## Running on [AWS](https://cloud.google.com/composer)

## Feedback

What did you think of this example? You can reach out to us on our [slack](http://bit.ly/MarquezSlack) channel and leave us feedback, or [open a pull request](https://github.com/MarquezProject/marquez/blob/main/CONTRIBUTING.md#submitting-a-pull-request) with your suggestions!  
