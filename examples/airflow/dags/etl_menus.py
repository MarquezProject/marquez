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
    'etl_menus',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Loads newly added restaurant menus daily.'
)

t1 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS menus (
      id            SERIAL PRIMARY KEY,
      name          VARCHAR(64) NOT NULL,
      restaurant_id INTEGER REFERENCES restaurants(id),
      description   TEXT,
      UNIQUE (name, restaurant_id)
    );''',
    dag=dag
)

t2 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO menus (id, name, restaurant_id, description)
      SELECT id, name, restaurant_id, description
        FROM tmp_menus;
    ''',
    dag=dag
)

t1 >> t2
