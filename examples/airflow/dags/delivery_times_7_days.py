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
    'delivery_times_7_days',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Determine weekly top delivery times by restaurant.'
)

t1 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS top_delivery_times (
      order_id            INTEGER REFERENCES orders(id),
      order_placed_on     TIMESTAMP NOT NULL,
      order_dispatched_on TIMESTAMP NOT NULL,
      order_delivered_on  TIMESTAMP NOT NULL,
      order_delivery_time DOUBLE PRECISION NOT NULL,
      customer_email      VARCHAR(64) NOT NULL,
      restaurant_id       INTEGER REFERENCES restaurants(id),
      driver_id           INTEGER REFERENCES drivers(id)
    );''',
    dag=dag
)

t2 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO top_delivery_times (
       order_id,
       order_placed_on,
       order_dispatched_on,
       order_delivered_on,
       order_delivery_time,
       customer_email,
       restaurant_id,
       driver_id
    ) SELECT order_id,
             order_placed_on,
             order_dispatched_on,
             order_delivered_on,
             EXTRACT(MINUTE FROM order_delivered_on) - EXTRACT(MINUTE FROM order_placed_on) AS order_delivery_time,
             customer_email,
             restaurant_id,
             driver_id
        FROM delivery_7_days
       ORDER BY order_delivery_time DESC
       LIMIT 1
    ''',
    dag=dag
)

t1 >> t2
