from marquez_airflow import DAG
from airflow.operators.postgres_operator import PostgresOperator
from airflow.operators.sensors import ExternalTaskSensor
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
    'email_discounts',
    schedule_interval='@once',
    catchup=False,
    is_paused_upon_creation=True,
    default_args=default_args,
    description='Email discounts to customers that have experienced order delays daily.'
)

# Wait for delivery_times_7_days DAG to complete
t1 = ExternalTaskSensor(
    task_id='wait_for_delivery_times_7_days',
    external_dag_id='delivery_times_7_days',
    mode='reschedule',
    dag=dag
)

t2 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    SELECT * FROM discounts;
    ''',
    dag=dag
)

t1 >> t2
