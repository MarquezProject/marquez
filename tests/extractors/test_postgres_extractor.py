# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import pytest

from airflow.operators.postgres_operator import PostgresOperator
from airflow.utils.dates import days_ago

from marquez_airflow import DAG
from marquez_airflow.extractors import get_extractors
from marquez_airflow.extractors import (Source, Dataset)

from marquez_client.models import (SourceType, DatasetType)

CONN_ID = 'food_delivery_db'
CONN_URI = 'postgres://localhost:5432/food_delivery'
DB_TABLE_NAME = 'discounts'

DEFAULT_ARGS = {
    'owner': 'datascience',
    'depends_on_past': False,
    'start_date': days_ago(7),
    'email_on_failure': False,
    'email_on_retry': False,
    'email': ['datascience@example.com']
}

DAG = dag = DAG(
    'food_delivery_7_days',
    schedule_interval='@weekly',
    default_args=DEFAULT_ARGS,
    description='Determines weekly food deliveries.'
)


@pytest.fixture
def extractors():
    return get_extractors()


def test_extract(extractors):
    expected_inputs = [
        Dataset(
            type=DatasetType.DB_TABLE,
            name=DB_TABLE_NAME,
            source=Source(
                type=SourceType.POSTGRESQL,
                name=CONN_ID,
                connection_url=CONN_URI
            )
        )]

    # Set the environment variable for the connection
    os.environ[f"AIRFLOW_CONN_{CONN_ID.upper()}"] = CONN_URI

    task = PostgresOperator(
        task_id='select',
        postgres_conn_id=CONN_ID,
        sql=f"SELECT * FROM {DB_TABLE_NAME};",
        dag=DAG
    )

    extractor = extractors.get(_extractor_key(task))
    # NOTE: When extracting operator metadata, only a single StepMetadata
    # object is returned. We'll want to cleanup the Extractor interface to
    # not return an array.
    step_metadata = extractor(task).extract()[0]

    assert step_metadata.name == 'food_delivery_7_days.select'
    assert step_metadata.inputs == expected_inputs
    assert step_metadata.outputs == []


def _extractor_key(task):
    return task.__class__
