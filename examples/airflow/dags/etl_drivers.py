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
    'etl_drivers',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Loads newly registered drivers daily.'
)

t1 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS drivers (
      id                SERIAL PRIMARY KEY,
      created_at        TIMESTAMP NOT NULL,
      updated_at        TIMESTAMP NOT NULL,
      name              VARCHAR(64) NOT NULL,
      email             VARCHAR(64) UNIQUE NOT NULL,
      phone             VARCHAR(64) NOT NULL,
      car_make          VARCHAR(64) NOT NULL,
      car_model         VARCHAR(64) NOT NULL,
      car_year          VARCHAR(64) NOT NULL,
      car_color         VARCHAR(64) NOT NULL,
      car_license_plate VARCHAR(64) NOT NULL
    );''',
    dag=dag
)

t2 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO drivers (id, created_at, updated_at, name, email, phone, car_make, car_model, car_year, car_color, car_license_plate)
      SELECT id, created_at, updated_at, name, email, phone, car_make, car_model, car_year, car_color, car_license_plate
        FROM tmp_drivers;
    ''',
    dag=dag
)

t1 >> t2
