import json
from unittest.mock import MagicMock

from marquez.dataset import Dataset, Source, DatasetType, Field

from marquez.provider.bigquery import BigQueryStatisticsProvider, BigQueryStatisticsRunFacet


def read_file_json(file):
    with open(file=file, mode='r') as f:
        return json.loads(f.read())


class TableMock(MagicMock):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.inputs = [
            read_file_json('tests/bigquery/table_details.json'),
            read_file_json("tests/bigquery/out_table_details.json")
        ]

    @property
    def _properties(self):
        return self.inputs.pop()


def test_bq_job_information():
    job_details = read_file_json('tests/bigquery/job_details.json')
    client = MagicMock()
    client.get_job.return_value._properties = job_details

    client.get_table.return_value = TableMock()

    statistics = BigQueryStatisticsProvider(client=client).get_statistics("job_id")

    assert statistics.run_facets == {
        'bigQuery_statistics': BigQueryStatisticsRunFacet(
            cached=False,
            billedBytes=111149056,
            properties=json.dumps(job_details)
        )
    }
    assert statistics.inputs == [
        Dataset(
            Source(
                'bq-airflow-marquez',
                'BIGQUERY',
                'bigquery:bigquery-public-data.usa_names.usa_1910_2013'
            ),
            name='bigquery-public-data.usa_names.usa_1910_2013',
            type=DatasetType.DB_TABLE,
            fields=[
                Field('state', 'STRING', [], '2-digit state code'),
                Field('gender', 'STRING', [], 'Sex (M=male or F=female)'),
                Field('year', 'INTEGER', [], '4-digit year of birth'),
                Field('name', 'STRING', [], 'Given name of a person at birth'),
                Field('number', 'INTEGER', [], 'Number of occurrences of the name')
            ]
        )
    ]
    assert statistics.output == Dataset(
        Source(
            'svc-bq-airflow-marquez@bq-airflow-marquez.iam.gserviceaccount.com',
            'BIGQUERY',
            'bigquery:bq-airflow-marquez.new_dataset.output_table'
        ),
        name='bq-airflow-marquez.new_dataset.output_table',
        type=DatasetType.DB_TABLE
    )
