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
    'email_discounts',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Email discounts to customers that have experienced order delays daily.'
)

t1 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    SELECT * FROM discounts;
    ''',
    dag=dag
)
