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
    'etl_order_status',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Loads order statues updates daily.'
)

# Wait for new_food_deliveries DAG to complete
t1 = ExternalTaskSensor(
    task_id='wait_for_new_food_deliveries',
    external_dag_id='new_food_deliveries',
    mode='reschedule',
    dag=dag
)

# Wait for etl_orders DAG to complete
t2 = ExternalTaskSensor(
    task_id='wait_for_etl_orders',
    external_dag_id='etl_orders',
    mode='reschedule',
    dag=dag
)

t3 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS order_status (
      id              SERIAL PRIMARY KEY,
      transitioned_at TIMESTAMP NOT NULL,
      status          VARCHAR(64),
      order_id        INTEGER REFERENCES orders(id),
      customer_id     INTEGER REFERENCES customers(id),
      restaurant_id   INTEGER REFERENCES restaurants(id),
      driver_id       INTEGER REFERENCES drivers(id)
    );''',
    dag=dag
)

t4 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO order_status (id, transitioned_at, status, order_id, customer_id, driver_id, restaurant_id)
      SELECT id, transitioned_at, status, order_id, customer_id, driver_id, restaurant_id
        FROM tmp_order_status;
    ''',
    dag=dag
)

t1 >> t3
t2 >> t3
t3 >> t4
