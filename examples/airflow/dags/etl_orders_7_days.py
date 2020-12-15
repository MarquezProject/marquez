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
    'etl_orders_7_days',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Loads newly placed orders weekly.'
)

# Wait for new_food_deliveries DAG to complete
t1 = ExternalTaskSensor(
    task_id='etl_orders_7_days_wait_for_new_food_deliveries',
    external_dag_id='new_food_deliveries',
    mode='reschedule',
    dag=dag
)

# Wait for etl_orders DAG to complete
t2 = ExternalTaskSensor(
    task_id='etl_orders_7_days_wait_for_etl_orders',
    external_dag_id='etl_orders',
    mode='reschedule',
    dag=dag
)

# Wait for etl_menus DAG to complete
t3 = ExternalTaskSensor(
    task_id='etl_orders_7_days_wait_for_etl_menus',
    external_dag_id='etl_menus',
    mode='reschedule',
    dag=dag
)

# Wait for etl_menu_items DAG to complete
t4 = ExternalTaskSensor(
    task_id='etl_orders_7_days_wait_for_etl_menu_items',
    external_dag_id='etl_menu_items',
    mode='reschedule',
    dag=dag
)

# Wait for etl_categories DAG to complete
t5 = ExternalTaskSensor(
    task_id='etl_orders_7_days_wait_for_etl_categories',
    external_dag_id='etl_categories',
    mode='reschedule',
    dag=dag
)

# Wait for etl_restaurants DAG to complete
t6 = ExternalTaskSensor(
    task_id='etl_orders_7_days_wait_for_etl_restaurants',
    external_dag_id='etl_restaurants',
    mode='reschedule',
    dag=dag
)

t7 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS orders_7_days (
      order_id      INTEGER REFERENCES orders(id),
      placed_on     TIMESTAMP NOT NULL,
      discount_id   INTEGER REFERENCES discounts(id),
      menu_id       INTEGER REFERENCES menus(id),
      restaurant_id INTEGER REFERENCES restaurants(id),
      menu_item_id  INTEGER REFERENCES menu_items(id),
      category_id   INTEGER REFERENCES categories(id)
    );''',
    dag=dag
)

t8 = PostgresOperator(
    task_id='tuncate',
    postgres_conn_id='food_delivery_db',
    sql='TRUNCATE TABLE orders_7_days;',
    dag=dag
)

t9 = PostgresOperator(
    task_id='insert',
    postgres_conn_id='food_delivery_db',
    sql='''
    INSERT INTO orders_7_days (order_id, placed_on, discount_id, menu_id, restaurant_id, menu_item_id, category_id)
      SELECT o.id AS order_id, o.placed_on, o.discount_id, m.id AS menu_id, m.restaurant_id, mi.id AS menu_item_id, c.id AS category_id
        FROM orders AS o
       INNER JOIN menu_items AS mi
          ON mi.id = o.menu_item_id
       INNER JOIN categories AS c
          ON c.id = mi.category_id
       INNER JOIN menus AS m
          ON m.id = c.menu_id
       WHERE o.placed_on >= NOW() - interval '7 days'
    ''',
    dag=dag
)

t1 >> t2 >> t3 >> t4 >> t5 >> t6 >> t7 >> t8 >> t9
