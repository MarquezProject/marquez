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
    'etl_menus',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Loads newly added restaurant menus daily.'
)

# Wait for new_food_deliveries DAG to complete
t1 = ExternalTaskSensor(
    task_id='wait_for_new_food_deliveries',
    external_dag_id='new_food_deliveries',
    mode='reschedule',
    dag=dag
)

# Wait for etl_restaurants DAG to complete
t2 = ExternalTaskSensor(
    task_id='wait_for_etl_restaurants',
    external_dag_id='etl_restaurants',
    mode='reschedule',
    dag=dag
)

t3 = PostgresOperator(
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

t4 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO menus (id, name, restaurant_id, description)
      SELECT id, name, restaurant_id, description
        FROM tmp_menus;
    ''',
    dag=dag
)

t1 >> t3
t2 >> t3
t3 >> t4
