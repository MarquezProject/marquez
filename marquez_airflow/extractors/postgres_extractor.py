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

import logging

from airflow.operators.postgres_operator import PostgresOperator

from marquez_airflow.utils import get_connection_uri
from marquez_airflow.extractors import BaseExtractor
from marquez_airflow.extractors import (Source, Dataset, StepMetadata)
from marquez_airflow.extractors.sql.experimental.parser import SqlParser

from marquez_client.models import (SourceType, DatasetType)

log = logging.getLogger(__name__)


class PostgresExtractor(BaseExtractor):
    operator_class = PostgresOperator

    def __init__(self, operator):
        super().__init__(operator)

    def extract(self) -> [StepMetadata]:
        # (1) Parse sql statement to obtain input / output tables.
        sql_meta = SqlParser.parse(self.operator.sql)
        log.info("postgres sql parse successful.")

        # (2) Default all inputs / outputs to current connection.
        # NOTE: We'll want to look into adding support for the `database`
        # property that is used to override the one defined in the connection.
        conn_id = self.operator.postgres_conn_id
        source = Source(
            type=SourceType.POSTGRESQL,
            name=conn_id,
            connection_url=get_connection_uri(conn_id))

        # (3) Map input / output tables to dataset objects with source set
        # as the current connection. NOTE: We may want to move _to_dataset()
        # to class BaseExtractor or a utils class.
        inputs = [
            self._to_dataset(source, table) for table in sql_meta.in_tables
        ]
        outputs = [
            self._to_dataset(source, table) for table in sql_meta.out_tables
        ]

        # TODO: Only return a single StepMetadata object.
        return [StepMetadata(
            # TODO: Define a common way across extractors to build the
            # job name for an operator
            name=f"{self.operator.dag_id}.{self.operator.task_id}",
            inputs=inputs,
            outputs=outputs,
            context={
                'sql': self.operator.sql
            }
        )]

    @staticmethod
    def _to_dataset(source, table) -> Dataset:
        return Dataset(
            type=DatasetType.DB_TABLE,
            name=table,
            source=source
        )
