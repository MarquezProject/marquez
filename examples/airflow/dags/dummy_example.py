from marquez_airflow import DAG
from airflow.operators.dummy_operator import DummyOperator
from datetime import datetime

default_args = {
    "owner": "airflow",
    "depends_on_past": False,
    "start_date": datetime(2019, 1, 1),
    "email": ["airflow@airflow.com"],
    "email_on_failure": False,
    "email_on_retry": False,
    
    'marquez_location': 'github://my_dag_location',
    'marquez_input_urns': ["s3://great_data", "s3://not_so_good_data"],
    'marquez_output_urns': ["s3://amazing_data"],
}

dag = DAG('dummy_operator_example', schedule_interval='*/10 * * * *',
          default_args=default_args, description="My awesome DAG")

run_this_1 = DummyOperator(task_id='run_this_1', dag=dag)
run_this_2 = DummyOperator(task_id='run_this_2', dag=dag)
run_this_2.set_upstream(run_this_1)
