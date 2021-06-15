import pytest
from openlineage.facet import DataSourceDatasetFacet, SchemaDatasetFacet, SchemaField

from marquez.models import DbTableName, DbColumn, DbTableSchema

from marquez.dataset import Source, Dataset, DatasetType

from openlineage.run import Dataset as OpenLineageDataset


@pytest.fixture
def source():
    return Source(
        type="DummySource",
        name="dummy_source_name",
        connection_url="http://dummy/source/url"
    )


@pytest.fixture
def table_schema():
    source = Source(
        type="DummySource",
        name="dummy_source_name",
        connection_url="http://dummy/source/url"
    )

    schema_name = 'public'
    table_name = DbTableName('discounts')
    columns = [
        DbColumn(
            name='id',
            type='int4',
            ordinal_position=1
        ),
        DbColumn(
            name='amount_off',
            type='int4',
            ordinal_position=2
        ),
        DbColumn(
            name='customer_email',
            type='varchar',
            ordinal_position=3
        ),
        DbColumn(
            name='starts_on',
            type='timestamp',
            ordinal_position=4
        ),
        DbColumn(
            name='ends_on',
            type='timestamp',
            ordinal_position=5
        )
    ]
    schema = DbTableSchema(
        schema_name=schema_name,
        table_name=table_name,
        columns=columns
    )
    return source, columns, schema


def test_dataset_from(source):
    dataset = Dataset.from_table(source, 'source_table', 'public')
    assert dataset == Dataset(source=source, type=DatasetType.DB_TABLE, name='public.source_table')


def test_dataset_to_openlineage(table_schema):
    namespace = 'namespace'
    source, columns, schema = table_schema

    dataset_schema = Dataset.from_table_schema(source, schema)
    assert dataset_schema.to_openlineage_dataset(namespace) == OpenLineageDataset(
        namespace=namespace,
        name='public.discounts',
        facets={
            'dataSource': DataSourceDatasetFacet(
                name='dummy_source_name',
                uri='http://dummy/source/url'
            ),
            'schema': SchemaDatasetFacet(
                fields=[
                    SchemaField(name='id', type='int4'),
                    SchemaField(name='amount_off', type='int4'),
                    SchemaField(name='customer_email', type='varchar'),
                    SchemaField(name='starts_on', type='timestamp'),
                    SchemaField(name='ends_on', type='timestamp')
                ]
            )
        }
    )
