
from datetime import datetime

from airflow.operators.dummy_operator import DummyOperator
from marquez_airflow import DAG

DAG_NAME = 'test_dag'

default_args = {
    'depends_on_past': False,
    'start_date': datetime(2019, 2, 1),
}

dag = DAG(DAG_NAME, schedule_interval='0 0 * * *',
          catchup=False,
          default_args=default_args, description="My awesome DAG")

run_this_1 = DummyOperator(task_id='run_this_1', dag=dag)
run_this_2 = DummyOperator(task_id='run_this_2', dag=dag)
run_this_2.set_upstream(run_this_1)
