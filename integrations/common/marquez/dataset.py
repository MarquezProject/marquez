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

from enum import Enum
from typing import List, Optional, Dict

from marquez.models import DbTableSchema, DbColumn
from openlineage.facet import BaseFacet, DataSourceDatasetFacet, DocumentationDatasetFacet, \
    SchemaDatasetFacet, SchemaField
from openlineage.run import Dataset as OpenLineageDataset


class DatasetType(Enum):
    DB_TABLE = "DB_TABLE"
    STREAM = "STREAM"


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
                 tags: List[str] = None, description: str = None):
        self.name = name
        self.type = type
        self.tags = tags
        self.description = description

        if self.tags is None:
            self.tags = []

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
                 fields: List[Field] = None, description: Optional[str] = None,
                 custom_facets: Dict[str, BaseFacet] = None):
        if fields is None:
            fields = []
        if custom_facets is None:
            custom_facets = {}
        self.source = source
        self.name = name
        self.type = type
        self.fields = fields
        self.description = description
        self.custom_facets = custom_facets

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

    def to_openlineage_dataset(self, namespace: str) -> OpenLineageDataset:
        facets = {
            "dataSource": DataSourceDatasetFacet(
                self.source.name,
                self.source.connection_url
            )
        }
        if self.description:
            facets.update({
                "documentation": DocumentationDatasetFacet(
                    description=self.description
                )
            })

        if self.fields is not None and len(self.fields):
            facets.update({
                "schema": SchemaDatasetFacet(
                    fields=[
                        SchemaField(field.name, field.type, field.description)
                        for field in self.fields
                    ]
                )
            })

        if self.custom_facets:
            facets.update(self.custom_facets)

        return OpenLineageDataset(
            namespace=namespace,
            name=self.name,
            facets=facets
        )
