from datetime import datetime

from airflow.operators.dummy_operator import DummyOperator
from marquez_airflow import DAG

dag = DAG(dag_id='test_dummy_dag',
          description='Test dummy DAG',
          schedule_interval='*/2 * * * *',
          start_date=datetime(2020, 1, 8),
          catchup=False,
          max_active_runs=1)


dummy_task = DummyOperator(
    task_id='test_dummy',
    dag=dag
)
