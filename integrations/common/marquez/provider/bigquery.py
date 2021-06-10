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
import attr

from typing import Tuple, Optional, Dict, List

from google.cloud import bigquery

from marquez.dataset import Dataset, Source
from marquez.models import DbTableSchema, DbColumn, DbTableName
from marquez.schema import GITHUB_LOCATION
from openlineage.facet import BaseFacet

from marquez.utils import get_from_nullable_chain

_BIGQUERY_CONN_URL = 'bigquery:{}'


@attr.s
class BigQueryErrorRunFacet(BaseFacet):
    """
    Represents errors that can happen during execution of BigqueryExtractor
    :param clientError: represents errors originating in bigquery client
    :param parserError: represents errors that happened during parsing SQL provided to bigquery
    """
    clientError: str = attr.ib(default=None)
    parserError: str = attr.ib(default=None)

    @staticmethod
    def _get_schema() -> str:
        return GITHUB_LOCATION + "bq-error-run-facet.json"


@attr.s
class BigQueryJobRunFacet(BaseFacet):
    """
    Facet that represents relevant statistics of bigquery run.
    :param cached: bigquery caches query results. Rest of the statistics will not be provided
        for cached queries.
    :param billedBytes: how many bytes bigquery bills for.
    :param properties: full property tree of bigquery run.
    """
    cached: bool = attr.ib()
    billedBytes: int = attr.ib(default=None)
    properties: str = attr.ib(default=None)

    @staticmethod
    def _get_schema() -> str:
        return GITHUB_LOCATION + "bq-statistics-run-facet.json"


@attr.s
class BigQueryStatisticsDatasetFacet(BaseFacet):
    """
    Facet that represents statistics of output dataset resulting from bigquery run.
    :param outputRows: how many rows query produced.
    :param size: size of output dataset in bytes.
    """
    rowCount: int = attr.ib()
    size: int = attr.ib()

    @staticmethod
    def _get_schema() -> str:
        return GITHUB_LOCATION + "bq-statistics-dataset-facet.json"


@attr.s
class BigQueryFacets:
    run_facets: Dict[str, BaseFacet] = attr.ib()
    inputs: List[Dataset] = attr.ib()
    output: Optional[Dataset] = attr.ib(default=None)


class BigQueryDatasetsProvider:
    def __init__(
        self,
        client: Optional[bigquery.Client] = None,
        logger: Optional[logging.Logger] = None
    ):
        self.client = client
        if client is None:
            self.client = bigquery.Client()
        self.logger = logger
        if logger is None:
            self.logger = logging.getLogger(__name__)

    def get_facets(self, job_id: str) -> BigQueryFacets:
        inputs = []
        output = None
        run_facets = {}
        try:
            try:
                job = self.client.get_job(job_id=job_id)
                props = job._properties

                run_stat_facet, dataset_stat_facet = self._get_output_statistics(props)

                run_facets.update({
                    "bigQuery_job": run_stat_facet
                })
                inputs = self._get_input_from_bq(props)
                output = self._get_output_from_bq(props)
                if output:
                    output.custom_facets.update({
                        "stats": dataset_stat_facet
                    })

            finally:
                # Ensure client has close() defined, otherwise ignore.
                # NOTE: close() was introduced in python-bigquery v1.23.0
                if hasattr(self.client, "close"):
                    self.client.close()
        except Exception as e:
            self.logger.error(
                f"Cannot retrieve job details from BigQuery.Client. {e}",
                exc_info=True
            )
            run_facets.update({
                "bigQuery_error": BigQueryErrorRunFacet(
                    clientError=f"{e}: {traceback.format_exc()}",
                )
            })
        return BigQueryFacets(run_facets, inputs, output)

    def _get_output_statistics(self, properties) \
            -> Tuple[BigQueryJobRunFacet, Optional[BigQueryStatisticsDatasetFacet]]:
        stages = get_from_nullable_chain(properties, ['statistics', 'query', 'queryPlan'])
        json_props = json.dumps(properties)

        if not stages:
            if get_from_nullable_chain(properties, ['statistics', 'query', 'statementType']) \
                    == 'CREATE_VIEW':
                return BigQueryJobRunFacet(cached=False), None

            # we're probably getting cached results
            if get_from_nullable_chain(properties, ['statistics', 'query', 'cacheHit']):
                return BigQueryJobRunFacet(cached=True), None
            if get_from_nullable_chain(properties, ['status', 'state']) != "DONE":
                raise ValueError("Trying to extract data from running bigquery job")
            raise ValueError(
                f"BigQuery properties did not have required data: queryPlan - {json_props}"
            )

        out_stage = stages[-1]
        out_rows = out_stage.get("recordsWritten", None)
        out_bytes = out_stage.get("shuffleOutputBytes", None)
        billed_bytes = get_from_nullable_chain(properties, [
            'statistics', 'query', 'totalBytesBilled'
        ])
        return BigQueryJobRunFacet(
            cached=False,
            billedBytes=int(billed_bytes) if billed_bytes else None,
            properties=json_props
        ), BigQueryStatisticsDatasetFacet(
            rowCount=int(out_rows),
            size=int(out_bytes)
        ) if out_bytes and out_rows else None

    def _get_input_from_bq(self, properties):
        bq_input_tables = get_from_nullable_chain(properties, [
            'statistics', 'query', 'referencedTables'
        ])
        if not bq_input_tables:
            return []

        input_table_names = [
            self._bq_table_name(bq_t) for bq_t in bq_input_tables
        ]
        sources = [
            self._source(
                bq_t,
                get_from_nullable_chain(properties, ['jobReference', 'projectId'])
            ) for bq_t in bq_input_tables
        ]
        try:
            return [
                Dataset.from_table_schema(
                    source=source,
                    table_schema=table_schema
                )
                for table_schema, source in zip(self._get_table_schemas(
                    input_table_names
                ), sources)
            ]
        except Exception as e:
            self.logger.warning(f'Could not extract schema from bigquery. {e}')
            return [
                Dataset.from_table(source, table)
                for table, source in zip(input_table_names, sources)
            ]

    def _get_output_from_bq(self, properties) -> Optional[Dataset]:
        bq_output_table = get_from_nullable_chain(properties, [
            'configuration', 'query', 'destinationTable'
        ])
        if not bq_output_table:
            return None

        output_table_name = self._bq_table_name(bq_output_table)
        source = self._source(
            bq_output_table,
            get_from_nullable_chain(properties, ['user_email'])
        )

        table_schema = self._get_table_safely(output_table_name)
        if table_schema:
            return Dataset.from_table_schema(
                source=source,
                table_schema=table_schema
            )
        else:
            self.logger.warning("Could not resolve output table from bq")
            return Dataset.from_table(source, output_table_name)

    def _get_table_safely(self, output_table_name):
        try:
            return self._get_table(output_table_name)
        except Exception as e:
            self.logger.warning(f'Could not extract output schema from bigquery. {e}')
        return None

    def _get_table_schemas(self, tables: [str]) \
            -> [DbTableSchema]:
        # Avoid querying BigQuery by returning an empty array
        # if no tables have been provided.
        if not tables:
            return []

        return [self._get_table(table) for table in tables]

    def _get_table(self, table: str) -> Optional[DbTableSchema]:
        bq_table = self.client.get_table(table)
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

        return DbTableSchema(
            schema_name=table.get('tableReference').get('projectId') + '.' +
            table.get('tableReference').get('datasetId'),
            table_name=DbTableName(table.get('tableReference').get('tableId')),
            columns=columns
        )

    def _source(self, bq_table, name) -> Source:
        return Source(
            type="BIGQUERY",
            name=name,
            connection_url=_BIGQUERY_CONN_URL.format(self._bq_table_name(bq_table)))

    def _bq_table_name(self, bq_table):
        project = bq_table.get('projectId')
        dataset = bq_table.get('datasetId')
        table = bq_table.get('tableId')
        return f"{project}.{dataset}.{table}"
