
from marquez.airflow import MarquezDag as DAG
from airflow.operators.dummy_operator import DummyOperator
from datetime import datetime
DAG_NAME = 'test_dag_v2'

default_args = {
    'marquez_location': 'github://my_dag_location',
    'marquez_input_urns': ["s3://great_data", "s3://not_so_good_data"],
    'marquez_output_urns': ["s3://amazing_data"],
    'owner': 'some dag developer',
    'depends_on_past': False,
    'start_date': datetime(2019, 1, 31),
}

dag = DAG(DAG_NAME, schedule_interval='*/10 * * * *',
          default_args=default_args, description="My awesome DAG")

run_this_1 = DummyOperator(task_id='run_this_1', dag=dag)
run_this_2 = DummyOperator(task_id='run_this_2', dag=dag)
run_this_2.set_upstream(run_this_1)
