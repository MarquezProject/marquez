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

    def extract(self) -> [StepMetadata]:
        sql_meta = SqlParser.parse(self.operator.sql)
        log.debug(f"bigquery sql parsed and obtained meta: {sql_meta}")

        # default value: 'bigquery_default'
        conn_id = self.operator.bigquery_conn_id
        source = Source(
            type="BIGQUERY",
            name=conn_id,
            connection_url=_BIGQUERY_CONN_URL)
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

    # convenience method
    def extract_on_complete(self, task_instance) -> [StepMetadata]:
        log.debug(f"extract_on_complete({task_instance})")

        bigquery_job_id = self._get_bigquery_job_id(task_instance)

        try:
            client = bigquery.Client()
            try:
                job = client.get_job(job_id=bigquery_job_id)
                job_properties_str = self._get_job_properties_str(job)
            finally:
                client.close()
            steps_meta = self._add_job_properties(job_properties_str)
        except Exception as e:
            log.error(f"Cannot retrieve job details from BigQuery.Client. {e}",
                      exc_info=True)

        return steps_meta

    def _add_job_properties(self, job_properties) -> [StepMetadata]:
        log.debug("extract_on_complete(job_details)")
        steps_meta = self.extract()

        steps_meta[0].context = {
            'sql': self.operator.sql,
            'bigquery.job_properties': job_properties
        }

        return steps_meta

    def _get_job_properties_str(self, job):
        job_properties = json.dumps(job._properties)

        log.debug(f"job: {job_properties}")

        return job_properties

    def _get_bigquery_job_id(self, task_instance):
        bigquery_job_id = task_instance.xcom_pull(
            task_ids=task_instance.task_id, key='job_id')

        log.info(f"bigquery_job_id: {bigquery_job_id}")

        return bigquery_job_id
