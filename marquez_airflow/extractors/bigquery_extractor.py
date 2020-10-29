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

import json
import logging

from airflow.contrib.operators.bigquery_operator import BigQueryOperator
from google.cloud import bigquery

from marquez_airflow.extractors import (
    BaseExtractor,
    StepMetadata,
    Source,
    Dataset
)
from marquez_airflow.extractors.sql.experimental.parser import SqlParser
from marquez_airflow.utils import (
    get_job_name
)

# BIGQUERY DAGs doesn't use this.
# Required to pass marquez server validation.
_BIGQUERY_CONN_URL = \
    'jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443'

log = logging.getLogger(__name__)


class BigQueryExtractor(BaseExtractor):
    operator_class = BigQueryOperator

    def __init__(self, operator):
        super().__init__(operator)

    def _source(self) -> Source:
        conn_id = self.operator.bigquery_conn_id
        return Source(
            type="BIGQUERY",
            name=conn_id,
            connection_url=_BIGQUERY_CONN_URL)

    def extract(self) -> [StepMetadata]:
        source = self._source()
        sql_meta = SqlParser.parse(self.operator.sql)
        log.debug(f"bigquery sql parsed and obtained meta: {sql_meta}")

        inputs = [
            Dataset.from_table_only(
                source, table
            ) for table in sql_meta.in_tables
        ]
        outputs = [
            Dataset.from_table_only(
                source, table
            ) for table in sql_meta.out_tables
        ]

        return [StepMetadata(
            name=get_job_name(task=self.operator),
            inputs=inputs,
            outputs=outputs,
            context={
                'sql': self.operator.sql
            }
        )]

    def _bq_table_name(self, bq_table):
        project = bq_table.get('projectId')
        dataset = bq_table.get('datasetId')
        table = bq_table.get('tableId')
        return f"{project}.{dataset}.{table}"

    # convenience method
    def extract_on_complete(self, task_instance) -> [StepMetadata]:
        log.debug(f"extract_on_complete({task_instance})")

        source = self._source()
        bigquery_job_id = self._get_bigquery_job_id(task_instance)

        try:
            client = bigquery.Client()
            try:
                job = client.get_job(job_id=bigquery_job_id)
                job_properties = job._properties
                job_properties_str = json.dumps(job_properties)
                bq_input_tables = job_properties.get('statistics')\
                    .get('query')\
                    .get('referencedTables')

                input_table_names = [
                    self._bq_table_name(bq_t) for bq_t in bq_input_tables
                ]
                inputs = [
                    Dataset.from_table(source, table)
                    for table in input_table_names
                ]
                bq_output_table = job_properties.get('configuration')\
                    .get('query')\
                    .get('destinationTable')
                output_table_name = self._bq_table_name(bq_output_table)
                outputs = [
                    Dataset.from_table(source, output_table_name)
                ]
                return [StepMetadata(
                    name=get_job_name(task=self.operator),
                    inputs=inputs,
                    outputs=outputs,
                    context={
                        'sql': self.operator.sql,
                        'bigquery.job_properties': job_properties_str
                    }
                )]
            finally:
                client.close()

        except Exception as e:
            log.error(f"Cannot retrieve job details from BigQuery.Client. {e}",
                      exc_info=True)
        return []

    def _get_bigquery_job_id(self, task_instance):
        bigquery_job_id = task_instance.xcom_pull(
            task_ids=task_instance.task_id, key='job_id')

        log.info(f"bigquery_job_id: {bigquery_job_id}")

        return bigquery_job_id
