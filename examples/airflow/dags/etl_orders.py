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
    'etl_orders',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Loads newly placed orders daily.'
)

t1 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS orders (
      id           SERIAL PRIMARY KEY,
      placed_on    TIMESTAMP NOT NULL,
      menu_item_id INTEGER REFERENCES menu_items(id),
      quantity     INTEGER NOT NULL,
      discount_id  INTEGER REFERENCES discounts(id),
      comment      TEXT
    );''',
    dag=dag
)

t2 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO orders (id, placed_on, menu_item_id, quantity, discount_id, comment)
      SELECT id, placed_on, menu_item_id, quantity, discount_id, comment
        FROM tmp_orders;
    ''',
    dag=dag
)

t1 >> t2
