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
    'etl_categories',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Loads newly added menus categories daily.'
)

# Wait for new_food_deliveries DAG to complete
t1 = ExternalTaskSensor(
    task_id='etl_categories_wait_for_new_food_deliveries',
    external_dag_id='new_food_deliveries',
    mode='reschedule',
    dag=dag
)

# Wait for etl_menus DAG to complete
t2 = ExternalTaskSensor(
    task_id='etl_categories_wait_for_etl_menus',
    external_dag_id='etl_menus',
    mode='reschedule',
    dag=dag
)

t3 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS categories (
      id          SERIAL PRIMARY KEY,
      name        VARCHAR(64) NOT NULL,
      menu_id     INTEGER REFERENCES menus(id),
      description TEXT,
      UNIQUE (name, menu_id)
    );''',
    dag=dag
)

t4 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO categories (id, name, menu_id, description)
      SELECT id, name, menu_id, description
        FROM tmp_categories;
    ''',
    dag=dag
)

t1 >> t2 >> t3 >> t4
