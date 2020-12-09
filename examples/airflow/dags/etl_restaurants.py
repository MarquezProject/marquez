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
    'etl_restaurants',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Loads newly registered restaurants daily.'
)

t1 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS restaurants (
      id                SERIAL PRIMARY KEY,
      created_at        TIMESTAMP NOT NULL,
      updated_at        TIMESTAMP NOT NULL,
      name              VARCHAR(64) NOT NULL,
      email             VARCHAR(64) UNIQUE NOT NULL,
      address           VARCHAR(64) NOT NULL,
      phone             VARCHAR(64) NOT NULL,
      city_id           INTEGER REFERENCES cities(id),
      business_hours_id INTEGER REFERENCES business_hours(id),
      description       TEXT
    );''',
    dag=dag
)

t2 = PostgresOperator(
    task_id='etl',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO restaurants (id, created_at, updated_at, name, email, address, phone, city_id, business_hours_id, description)
      SELECT id, created_at, updated_at, name, email, address, phone, city_id, business_hours_id, description
        FROM tmp_restaurants;
    ''',
    dag=dag
)

t1 >> t2
