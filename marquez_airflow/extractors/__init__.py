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

from typing import List
from abc import ABC, abstractmethod

from airflow import LoggingMixin
from airflow.models import BaseOperator

from marquez_airflow.models import DbTableSchema, DbColumn

from marquez_client.models import DatasetType


class Source:
    name = None
    connection_url = None
    type = None

    def __init__(self, name, type, connection_url):
        self.name = name
        self.type = type
        self.connection_url = connection_url

    def __eq__(self, other):
        return self.name == other.name and \
               self.type == other.type and \
               self.connection_url == other.connection_url

    def __repr__(self):
        return f"Source({self.name!r},{self.type!r},{self.connection_url!r})"


class Field:
    def __init__(self, name: str, type: str,
                 tags: List[str] = [], description: str = None):
        self.name = name
        self.type = type
        self.tags = tags
        self.description = description

    @staticmethod
    def from_column(column: DbColumn):
        return Field(
            name=column.name,
            type=column.type,
            description=column.description
        )

    def __eq__(self, other):
        return self.name == other.name and \
               self.type == other.type and \
               self.tags == other.tags and \
               self.description == other.description

    def __repr__(self):
        return f"Field({self.name!r},{self.type!r}, \
                       {self.tags!r},{self.description!r})"


class Dataset:
    def __init__(self, source: Source, name: str, type: DatasetType,
                 fields: List[Field] = [], description=None):
        self.source = source
        self.name = name
        self.type = type
        self.fields = fields
        self.description = description

    @staticmethod
    def from_table(source: Source, table_name: str,
                   schema_name: str = None):
        return Dataset(
            type=DatasetType.DB_TABLE,
            name=Dataset._to_name(
                schema_name=schema_name,
                table_name=table_name
            ),
            source=source
        )

    @staticmethod
    def from_table_schema(source: Source, table_schema: DbTableSchema):
        return Dataset(
            type=DatasetType.DB_TABLE,
            name=Dataset._to_name(
                schema_name=table_schema.schema_name,
                table_name=table_schema.table_name.name
            ),
            source=source,
            fields=[
                # We want to maintain column order using ordinal position.
                Field.from_column(column) for column in sorted(
                    table_schema.columns, key=lambda x: x.ordinal_position
                )
            ]
        )

    @staticmethod
    def _to_name(table_name: str, schema_name: str = None):
        # Prefix the table name with the schema name using
        # the format: {table_schema}.{table_name}.
        return f"{schema_name}.{table_name}" if schema_name else table_name

    def __eq__(self, other):
        return self.source == other.source and \
               self.name == other.name and \
               self.type == other.type and \
               self.fields == other.fields and \
               self.description == other.description

    def __repr__(self):
        return f"Dataset({self.source!r},{self.name!r}, \
                         {self.type!r},{self.fields!r},{self.description!r})"


class StepMetadata:
    # TODO: Define a common way across extractors to build the
    # job name for an operator
    name = None
    location = None
    inputs = []
    outputs = []
    context = {}

    def __init__(self, name, location=None, inputs=None, outputs=None,
                 context=None):
        self.name = name
        self.location = location
        if inputs:
            self.inputs = inputs
        if outputs:
            self.outputs = outputs
        if context:
            self.context = context

    def __repr__(self):
        return "name: {}\t inputs: {} \t outputs: {}".format(
            self.name,
            ','.join([str(i) for i in self.inputs]),
            ','.join([str(o) for o in self.outputs]))


class BaseExtractor(ABC, LoggingMixin):
    operator: BaseOperator = None
    operator_class = None

    def __init__(self, operator):
        self.operator = operator

    @classmethod
    def get_operator_class(cls):
        return cls.operator_class

    def validate(self):
        # TODO: maybe we should also enforce the module
        assert (self.operator_class is not None and
                self.operator.__class__ == self.operator_class)

    # TODO: Only return a single StepMetadata object.
    @abstractmethod
    def extract(self) -> [StepMetadata]:
        pass

    def extract_on_complete(self, task_instance) -> [StepMetadata]:
        # TODO: This method allows for the partial updating of task
        # metadata on completion. Marquez currently doesn't support
        # partial updates within the context of a DAG run, but this feature
        # will soon be supported:
        # https://github.com/MarquezProject/marquez/issues/816
        #
        # Also, we'll want to revisit the metadata extraction flow,
        # but for now, return an empty set as the default behavior
        # as not all extractors need to handle partial metadata updates.
        return self.extract()
