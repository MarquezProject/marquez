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
import traceback
from typing import Optional, Any, List, Mapping

from airflow.contrib.operators.bigquery_operator import BigQueryOperator
from google.cloud import bigquery
import attr

from marquez_airflow.extractors import (
    BaseExtractor,
    StepMetadata,
    Source,
    Dataset
)
from marquez_airflow.models import (
    DbTableName,
    DbTableSchema,
    DbColumn
)
from marquez_airflow.extractors.sql.experimental.parser import SqlParser
from marquez_airflow.schema import GITHUB_LOCATION
from marquez_airflow.utils import (
    get_job_name
)

# BIGQUERY DAGs doesn't use this.
# Required to pass marquez server validation.
from openlineage.facet import BaseFacet

_BIGQUERY_CONN_URL = \
    'jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443'

log = logging.getLogger(__name__)


def get_from_nullable_chain(source: Mapping[str, Any], chain: List[str]) -> Optional[Any]:
    chain.reverse()
    try:
        while chain:
            source = source.get(chain.pop())
        return source
    except AttributeError:
        return


@attr.s
class BigQueryErrorRunFacet(BaseFacet):
    clientError: str = attr.ib(default=None)
    schemaError: str = attr.ib(default=None)
    parserError: str = attr.ib(default=None)

    @staticmethod
    def _get_schema() -> str:
        return GITHUB_LOCATION + "bq-error-run-facet.json"


@attr.s
class BigQueryStaticticsRunFacet(BaseFacet):
    cached: bool = attr.ib()
    outputRows: int = attr.ib(default=None)
    billedBytes: int = attr.ib(default=None)
    properties: str = attr.ib(default=None)

    @staticmethod
    def _get_schema() -> str:
        return GITHUB_LOCATION + "bq-statistics-run-facet.json"


@attr.s
class SqlContext:
    sql: str = attr.ib()
    inputs: Optional[str] = attr.ib(default=None)
    outputs: Optional[str] = attr.ib(default=None)
    parser_error: Optional[str] = attr.ib(default=None)


class BigQueryExtractor(BaseExtractor):
    operator_class = BigQueryOperator

    def __init__(self, operator: BigQueryOperator):
        super().__init__(operator)

    def _source(self) -> Source:
        conn_id = self.operator.bigquery_conn_id
        return Source(
            type="BIGQUERY",
            name=conn_id,
            connection_url=_BIGQUERY_CONN_URL)

    def extract(self) -> StepMetadata:
        return None

    def _bq_table_name(self, bq_table):
        project = bq_table.get('projectId')
        dataset = bq_table.get('datasetId')
        table = bq_table.get('tableId')
        return f"{project}.{dataset}.{table}"

    def extract_on_complete(self, task_instance) -> StepMetadata:
        log.debug(f"extract_on_complete({task_instance})")
        context = self.parse_sql_context()
        source = self._source()

        try:
            bigquery_job_id = self._get_xcom_bigquery_job_id(task_instance)
            if bigquery_job_id is None:
                raise Exception("Xcom could not resolve BigQuery job id." +
                                "Job may have failed.")
        except Exception as e:
            log.error(f"Cannot retrieve job details from BigQuery.Client. {e}",
                      exc_info=True)
            return StepMetadata(
                name=get_job_name(task=self.operator),
                inputs=None,
                outputs=None,
                run_facets=[
                    BigQueryErrorRunFacet(
                        clientError=f"{e}: {traceback.format_exc()}",
                        parserError=context.parser_error
                    )
                ]
            )

        inputs = None
        outputs = None
        run_facets = []
        try:
            client = bigquery.Client()
            try:
                job = client.get_job(job_id=bigquery_job_id)
                props = job._properties

                run_facets.append(self._get_output_statistics(props))

                inputs = self._get_input_from_bq(props, source, client)
                outputs = self._get_output_from_bq(props, source, client)
            finally:
                # Ensure client has close() defined, otherwise ignore.
                # NOTE: close() was introduced in python-bigquery v1.23.0
                if hasattr(client, "close"):
                    client.close()
        except Exception as e:
            log.error(f"Cannot retrieve job details from BigQuery.Client. {e}",
                      exc_info=True)
            run_facets.append(BigQueryErrorRunFacet(
                clientError=f"{e}: {traceback.format_exc()}",
                parserError=context.parser_error
            ))

        return StepMetadata(
            name=get_job_name(task=self.operator),
            inputs=inputs,
            outputs=outputs,
            run_facets=run_facets
        )

    def _get_output_statistics(self, properties):
        stages = get_from_nullable_chain(properties, ['statistics', 'query', 'queryPlan'])
        json_props = json.dumps(properties)

        if not stages:
            # we're probably getting cached results
            if get_from_nullable_chain(properties, ['statistics', 'query', 'cacheHit']):
                return BigQueryStaticticsRunFacet(cached=True)
            if get_from_nullable_chain(properties, ['status', 'state']) != "DONE":
                raise ValueError("Trying to extract data from running bigquery job")
            raise ValueError(
                f"BigQuery properties did not have required data: queryPlan - {json_props}"
            )

        out_stage = stages[-1]
        out_rows = out_stage.get("recordsWritten", None)
        billed_bytes = get_from_nullable_chain(properties, [
            'statistics', 'query', 'totalBytesBilled'
        ])
        return BigQueryStaticticsRunFacet(
            cached=False,
            outputRows=int(out_rows) if out_rows else None,
            billedBytes=int(billed_bytes) if billed_bytes else None,
            properties=json_props
        )

    def _get_input_from_bq(self, properties, source, client):
        bq_input_tables = get_from_nullable_chain(properties, [
            'statistics', 'query', 'referencedTables'
        ])
        if not bq_input_tables:
            return None

        input_table_names = [
            self._bq_table_name(bq_t) for bq_t in bq_input_tables
        ]
        try:
            return [
                Dataset.from_table_schema(
                    source=source,
                    table_schema=table_schema
                )
                for table_schema in self._get_table_schemas(
                    input_table_names, client
                )
            ]
        except Exception as e:
            log.warning(f'Could not extract schema from bigquery. {e}')
            return [
                Dataset.from_table(source, table)
                for table in input_table_names
            ]

    def _get_output_from_bq(self, properties, source, client):
        bq_output_table = get_from_nullable_chain(properties, [
            'configuration', 'query', 'destinationTable'
        ])
        if not bq_output_table:
            return None

        output_table_name = self._bq_table_name(bq_output_table)
        table_schema = self._get_table_safely(output_table_name, client)
        if table_schema:
            return [Dataset.from_table_schema(
                source=source,
                table_schema=table_schema
            )]
        else:
            log.warning("Could not resolve output table from bq")
            return [
                Dataset.from_table(source, output_table_name)
            ]

    def _get_table_safely(self, output_table_name, client):
        try:
            return self._get_table(output_table_name, client)
        except Exception as e:
            log.warning(f'Could not extract output schema from bigquery. {e}')
        return None

    def _get_table_schemas(self, tables: [str], client: bigquery.Client) \
            -> [DbTableSchema]:
        # Avoid querying BigQuery by returning an empty array
        # if no tables have been provided.
        if not tables:
            return []

        return [self._get_table(table, client) for table in tables]

    def _get_table(self, table: str, client: bigquery.Client) -> Optional[DbTableSchema]:
        bq_table = client.get_table(table)
        if not bq_table._properties:
            return
        table = bq_table._properties

        fields = get_from_nullable_chain(table, ['schema', 'fields'])
        if not fields:
            return

        columns = [DbColumn(
            name=fields[i].get('name'),
            type=fields[i].get('type'),
            description=fields[i].get('description'),
            ordinal_position=i
        ) for i in range(len(fields))]
        self.log.info(DbTableName(table.get('tableReference').get('tableId')))

        return DbTableSchema(
            schema_name=table.get('tableReference').get('projectId') + '.' +
            table.get('tableReference').get('datasetId'),
            table_name=DbTableName(table.get('tableReference').get('tableId')),
            columns=columns
        )

    def _get_xcom_bigquery_job_id(self, task_instance):
        bigquery_job_id = task_instance.xcom_pull(
            task_ids=task_instance.task_id, key='job_id')

        log.info(f"bigquery_job_id: {bigquery_job_id}")
        return bigquery_job_id

    def parse_sql_context(self) -> SqlContext:
        try:
            sql_meta = SqlParser.parse(self.operator.sql)
            log.debug(f"bigquery sql parsed and obtained meta: {sql_meta}")
            return SqlContext(
                sql=self.operator.sql,
                inputs=json.dumps(
                    [in_table.name for in_table in sql_meta.in_tables]
                ),
                outputs=json.dumps(
                    [out_table.name for out_table in sql_meta.out_tables]
                )
            )
        except Exception as e:
            log.error(f"Cannot parse sql query. {e}",
                      exc_info=True)
            return SqlContext(
                sql=self.operator.sql,
                parser_error=f'{e}: {traceback.format_exc()}'
            )
