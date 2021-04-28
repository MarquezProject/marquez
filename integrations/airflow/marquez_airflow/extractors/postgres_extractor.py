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
from contextlib import closing
from typing import Optional

from airflow.hooks.postgres_hook import PostgresHook
from airflow.operators.postgres_operator import PostgresOperator

from marquez_airflow.models import (
    DbTableName,
    DbTableSchema,
    DbColumn
)
from marquez_airflow.utils import get_connection_uri
from marquez_airflow.extractors.sql import SqlMeta
from marquez_airflow.extractors.sql import SqlParser
from marquez_airflow.extractors import (
    BaseExtractor,
    StepMetadata,
    Source,
    Dataset
)

_TABLE_SCHEMA = 0
_TABLE_NAME = 1
_COLUMN_NAME = 2
_ORDINAL_POSITION = 3
# Use 'udt_name' which is the underlying type of column
# (ex: int4, timestamp, varchar, etc)
_UDT_NAME = 4


class PostgresExtractor(BaseExtractor):
    operator_class = PostgresOperator
    default_schema = 'public'
    source_type = 'POSTGRESQL'

    def __init__(self, operator):
        super().__init__(operator)

    def extract(self) -> StepMetadata:
        # (1) Parse sql statement to obtain input / output tables.
        sql_meta: SqlMeta = SqlParser.parse(self.operator.sql, self.default_schema)

        # (2) Default all inputs / outputs to current connection.
        # NOTE: We'll want to look into adding support for the `database`
        # property that is used to override the one defined in the connection.
        conn_id = self._conn_id()
        source = Source(
            type=self.source_type,
            name=conn_id,
            connection_url=get_connection_uri(conn_id))

        # (3) Map input / output tables to dataset objects with source set
        # as the current connection. We need to also fetch the schema for the
        # input tables to format the dataset name as:
        # {schema_name}.{table_name}
        inputs = [
            Dataset.from_table(
                source=source,
                table_name=in_table_schema.table_name.name,
                schema_name=in_table_schema.schema_name
            ) for in_table_schema in self._get_table_schemas(
                sql_meta.in_tables
            )
        ]
        outputs = [
            Dataset.from_table_schema(
                source=source,
                table_schema=out_table_schema
            ) for out_table_schema in self._get_table_schemas(
                sql_meta.out_tables
            )
        ]

        return StepMetadata(
            name=f"{self.operator.dag_id}.{self.operator.task_id}",
            inputs=inputs,
            outputs=outputs,
            context={
                'sql': self.operator.sql
            }
        )

    def _conn_id(self):
        return self.operator.postgres_conn_id

    def _information_schema_query(self, table_names: str) -> str:
        return f"""
        SELECT table_schema,
        table_name,
        column_name,
        ordinal_position,
        udt_name
        FROM information_schema.columns
        WHERE table_name IN ({table_names});
        """

    def _get_hook(self):
        return PostgresHook(
            postgres_conn_id=self.operator.postgres_conn_id,
            schema=self.operator.database
        )

    def _get_table_schemas(
            self, table_names: [DbTableName]
    ) -> [DbTableSchema]:
        # Avoid querying postgres by returning an empty array
        # if no table names have been provided.
        if not table_names:
            return []

        # Keeps tack of the schema by table.
        schemas_by_table = {}

        hook = self._get_hook()
        with closing(hook.get_conn()) as conn:
            with closing(conn.cursor()) as cursor:
                table_names_as_str = ",".join(map(
                    lambda name: f"'{name.name}'", table_names
                ))
                cursor.execute(
                    self._information_schema_query(table_names_as_str)
                )
                for row in cursor.fetchall():
                    table_schema_name: str = row[_TABLE_SCHEMA]
                    table_name: DbTableName = DbTableName(row[_TABLE_NAME])
                    table_column: DbColumn = DbColumn(
                        name=row[_COLUMN_NAME],
                        type=row[_UDT_NAME],
                        ordinal_position=row[_ORDINAL_POSITION]
                    )

                    # Attempt to get table schema
                    table_key: str = f"{table_schema_name}.{table_name}"
                    table_schema: Optional[DbTableSchema] = schemas_by_table.get(table_key)

                    if table_schema:
                        # Add column to existing table schema.
                        schemas_by_table[table_key].columns.append(table_column)
                    else:
                        # Create new table schema with column.
                        schemas_by_table[table_key] = DbTableSchema(
                            schema_name=table_schema_name,
                            table_name=table_name,
                            columns=[table_column]
                        )

        return list(schemas_by_table.values())
