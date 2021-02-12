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
    'email': ['datascience@example.com']
}

dag = DAG(
    'new_food_deliveries',
    schedule_interval='@hourly',
    catchup=False,
    default_args=default_args,
    description='Add new food delivery data.'
)


t1 = PostgresOperator(
    task_id='if_not_exists',
    postgres_conn_id='food_delivery_db',
    sql='''
    CREATE TABLE IF NOT EXISTS cities (
      id       SERIAL PRIMARY KEY,
      name     VARCHAR(64) NOT NULL,
      state    VARCHAR(64) NOT NULL,
      zip_code VARCHAR(64) NOT NULL,
      UNIQUE (name, state, zip_code)
    );
    CREATE TABLE IF NOT EXISTS business_hours (
      id          SERIAL PRIMARY KEY,
      day_of_week VARCHAR(64) NOT NULL,
      opens_at    TIMESTAMP NOT NULL,
      closes_at   TIMESTAMP NOT NULL
    );
    CREATE TABLE IF NOT EXISTS discounts (
      id           SERIAL PRIMARY KEY,
      amount_off   INTEGER,
      customers_id INTEGER NOT NULL,
      starts_at    TIMESTAMP NOT NULL,
      ends_at      TIMESTAMP NOT NULL
     );
    CREATE TABLE IF NOT EXISTS tmp_restaurants (
      id                SERIAL PRIMARY KEY,
      created_at        TIMESTAMP NOT NULL,
      updated_at        TIMESTAMP NOT NULL,
      name              VARCHAR(64) NOT NULL,
      email             VARCHAR(64) UNIQUE NOT NULL,
      address           VARCHAR(64) NOT NULL,
      phone             VARCHAR(64) NOT NULL,
      city_id           INTEGER NOT NULL,
      business_hours_id INTEGER NOT NULL,
      description       TEXT
    );
    CREATE TABLE IF NOT EXISTS tmp_menus (
      id            SERIAL PRIMARY KEY,
      name          VARCHAR(64) NOT NULL,
      restaurant_id INTEGER NOT NULL,
      description   TEXT
    );
    CREATE TABLE IF NOT EXISTS tmp_menu_items (
      id          SERIAL PRIMARY KEY,
      name        VARCHAR(64) NOT NULL,
      price       VARCHAR(64) NOT NULL,
      category_id INTEGER,
      description TEXT
    );
    CREATE TABLE IF NOT EXISTS tmp_categories (
      id          SERIAL PRIMARY KEY,
      name        VARCHAR(64) NOT NULL,
      menu_id     INTEGER NOT NULL,
      description TEXT,
      UNIQUE (name, menu_id)
    );
    CREATE TABLE IF NOT EXISTS tmp_drivers (
      id                SERIAL PRIMARY KEY,
      created_at        TIMESTAMP NOT NULL,
      updated_at        TIMESTAMP NOT NULL,
      name              VARCHAR(64) NOT NULL,
      email             VARCHAR(64) UNIQUE NOT NULL,
      phone             VARCHAR(64) NOT NULL,
      car_make          VARCHAR(64) NOT NULL,
      car_model         VARCHAR(64) NOT NULL,
      car_year          VARCHAR(64) NOT NULL,
      car_color         VARCHAR(64) NOT NULL,
      car_license_plate VARCHAR(64) NOT NULL
    );
    CREATE TABLE IF NOT EXISTS tmp_customers (
      id         SERIAL PRIMARY KEY,
      created_at TIMESTAMP NOT NULL,
      updated_at TIMESTAMP NOT NULL,
      name       VARCHAR(64) NOT NULL,
      email      VARCHAR(64) UNIQUE NOT NULL,
      address    VARCHAR(64) NOT NULL,
      phone      VARCHAR(64) NOT NULL,
      city_id    INTEGER
    );
    CREATE TABLE IF NOT EXISTS tmp_orders (
      id           SERIAL PRIMARY KEY,
      placed_on    TIMESTAMP NOT NULL,
      menu_item_id INTEGER NOT NULL,
      quantity     INTEGER NOT NULL,
      discount_id  INTEGER NOT NULL,
      comment      TEXT
    ); 
    CREATE TABLE IF NOT EXISTS tmp_order_status (
      id              SERIAL PRIMARY KEY,
      transitioned_at TIMESTAMP NOT NULL,
      status          VARCHAR(64),
      order_id        INTEGER NOT NULL,
      customer_id     INTEGER NOT NULL,
      restaurant_id   INTEGER NOT NULL,
      driver_id       INTEGER NOT NULL
    );
    ''',
    dag=dag
)
