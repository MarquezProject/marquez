import unittest

from marquez_client import MarquezClient
from marquez_client.models import DatasetType, SourceType, JobType, RunState
import mock
import uuid
import pytz
from pyrfc3339 import generate
import datetime

_NAMESPACE = "my-namespace"


class TestMarquezClient(unittest.TestCase):
    def setUp(self):
        self.client = MarquezClient()

    @mock.patch("marquez_client.MarquezClient._put")
    def test_create_namespace(self, mock_put):
        owner_name = "me"
        description = "my namespace for testing."

        mock_put.return_value = {
            "name": _NAMESPACE,
            "ownerName": owner_name,
            "description": description
        }

        response = self.client.create_namespace(
            _NAMESPACE, owner_name, description)

        assert _NAMESPACE == str(response['name'])
        assert owner_name == str(response['ownerName'])
        assert description == str(response['description'])

    @mock.patch("marquez_client.MarquezClient._put")
    def test_create_dataset(self, mock_put):
        dataset_name = "my-dataset"
        description = "My dataset for testing."

        fields = [
            {
                "name": "flight_id",
                "type": "INTEGER",
                "description": "flight id"
            },
            {
                "name": "flight_name",
                "type": "VARCHAR",
                "description": "flight name"
            },
            {
                "name": "flight_date",
                "type": "TIMESTAMP",
                "description": "flight date"
            }
        ]

        mock_put.return_value = {
            'id': {
                'namespace': 'my-namespace',
                'name': 'my-dataset'
            },
            'type': 'DB_TABLE',
            'name': 'my-dataset',
            'physicalName': 'public.mytable',
            'createdAt': '2020-08-12T05:46:31.172877Z',
            'updatedAt': '2020-08-12T05:46:31.184934Z',
            'namespace': 'my-namespace',
            'sourceName': 'mydb',
            'fields': [
                {
                    'name': 'my_date',
                    'type': 'TIMESTAMP',
                    'description': 'my date'
                },
                {
                    'name': 'my_id',
                    'type': 'INTEGER',
                    'description': 'my id'
                },
                {
                    'name': 'my_name',
                    'type': 'VARCHAR',
                    'description': 'my name'
                }
            ],
            'tags': [],
            'lastModifiedAt': None,
            'description': 'My dataset for testing.'
        }

        response = self.client.create_dataset(
            namespace_name=_NAMESPACE,
            dataset_name=dataset_name,
            dataset_type=DatasetType.DB_TABLE,
            physical_name=dataset_name,
            source_name='my-source',
            description=description,
            run_id=None,
            schema_location=None,
            fields=fields,
            tags=None
        )

        assert str(response['description']) == description
        assert str(response['name']) == dataset_name

    @mock.patch("marquez_client.MarquezClient._put")
    def test_create_datasource(self, mock_put):
        source_name = "flight_schedules_db"
        source_type = SourceType.POSTGRESQL
        source_url = "jdbc:postgresql://localhost:5432/test?" \
                     "user=fred&password=secret&ssl=true"
        description = "PostgreSQL - flight schedules database"

        mock_put.return_value = {
            "type": "POSTGRESQL",
            "name": "flight_schedules_db",
            "createdAt": "2020-08-11T20:15:04.603158Z",
            "updatedAt": "2020-08-11T20:15:04.603158Z",
            "connectionUrl": "jdbc:postgresql://localhost:5432/"
                             "test?user=fred&password=secret&ssl=true",
            "description": "PostgreSQL - flight schedules database"
        }

        response = self.client.create_source(
            source_name=source_name,
            source_type=source_type,
            connection_url=source_url,
            description=description)

        assert response['name'] == source_name
        assert response['connectionUrl'] == source_url
        assert response['type'] == source_type.value

    @mock.patch("marquez_client.MarquezClient._put")
    def test_create_job(self, mock_put):
        job_name = "my-job"
        input_dataset = [
            {
                "namespace": "my-namespace",
                "name": "public.mytable"
            }
        ]
        output_dataset = {
            "namespace": "my-namespace",
            "name": "public.mytable"
        }

        location = "https://github.com/my-jobs/blob/" \
                   "07f3d2dfc8186cadae9146719e70294a4c7a8ee8"

        context = {
            "SQL": "SELECT * FROM public.mytable;"
        }

        mock_put.return_value = {
            "id": {
                "namespace": "my-namespace",
                "name": "my-job"
            },
            "type": "BATCH",
            "name": "my-job",
            "createdAt": "2020-08-12T07:30:55.321059Z",
            "updatedAt": "2020-08-12T07:30:55.333230Z",
            "namespace": "my-namespace",
            "inputs": [
                {
                    "namespace": "my-namespace",
                    "name": "public.mytable"
                }
            ],
            "outputs": [
                {
                    "namespace": "my-namespace",
                    "name": "public.mytable"
                }
            ],
            "location": "https://github.com/my-jobs/blob/"
                        "07f3d2dfc8186cadae9146719e70294a4c7a8ee8",
            "context": {
                "SQL": "SELECT * FROM public.mytable;"
            },
            "description": "My first job.",
            "latestRun": None
        }

        response = self.client.create_job(
            namespace_name=_NAMESPACE,
            job_name=job_name,
            job_type=JobType.BATCH,
            location=location,
            input_dataset=input_dataset,
            output_dataset=output_dataset,
            context=context
        )

        assert str(response['id']) is not None
        assert str(response['location']) == location

    @mock.patch("marquez_client.MarquezClient._post")
    def test_create_job_run(self, mock_post):
        job_name = "my-job"
        run_args = {
            "email": "me@mycorp.com",
            "emailOnFailure": "true",
            "emailOnRetry": "true",
            "retries": "1"
        }
        created_at = str(generate(datetime.datetime.utcnow()
                                  .replace(tzinfo=pytz.utc)))

        mock_post.return_value = {
            'id': f'{uuid.uuid4()}',
            'createdAt': f'{created_at}',
            'updatedAt': '2020-08-12T22:33:02.787228Z',
            'nominalStartTime': None,
            'nominalEndTime': None,
            'state': 'NEW',
            'startedAt': None,
            'endedAt': None,
            'durationMs': None,
            'run_args': {
                "email": "me@mycorp.com",
                "emailOnFailure": "true",
                "emailOnRetry": "true",
                "retries": "1"
            }
        }

        response = self.client.create_job_run(
            namespace_name=_NAMESPACE,
            job_name=job_name,
            nominal_start_time=None,
            nominal_end_time=None,
            run_args=run_args,
            mark_as_running=False
        )

        assert response['id'] is not None
        assert str(response['run_args']) == str(run_args)
        assert str(response['createdAt']) == created_at

    @mock.patch("marquez_client.MarquezClient._post")
    def test_mark_job_run_as_start(self, mock_post):
        run_id = str(uuid.uuid4())

        mock_post.return_value = {
            'id': f'{run_id}',
            'createdAt': '2020-08-12T22:36:50.739951Z',
            'updatedAt': '2020-08-13T17:56:39.516802Z',
            'nominalStartTime': None,
            'nominalEndTime': None,
            'state': 'RUNNING',
            'startedAt': '2020-08-13T17:56:39.516802Z',
            'endedAt': None,
            'durationMs': None,
            'args': {}
        }

        response = self.client.mark_job_run_as_started(run_id=run_id)

        assert str(response['id']) == run_id
        assert str(response['state']) == RunState.RUNNING.value

    @mock.patch("marquez_client.MarquezClient._post")
    def test_mark_job_run_as_completed(self, mock_post):
        run_id = str(uuid.uuid4())

        mock_post.return_value = {
            'id': f'{run_id}',
            'createdAt': '2020-08-12T22:36:50.739951Z',
            'updatedAt': '2020-08-13T17:56:39.516802Z',
            'nominalStartTime': None,
            'nominalEndTime': None,
            'state': 'COMPLETED',
            'startedAt': '2020-08-13T17:56:39.516802Z',
            'endedAt': None,
            'durationMs': None,
            'args': {}
        }

        response = self.client.mark_job_run_as_completed(run_id=run_id)

        assert str(response['id']) == run_id
        assert str(response['state']) == RunState.COMPLETED.value

    @mock.patch("marquez_client.MarquezClient._post")
    def test_mark_job_run_as_failed(self, mock_post):
        run_id = str(uuid.uuid4())

        mock_post.return_value = {
            'id': f'{run_id}',
            'createdAt': '2020-08-12T22:36:50.739951Z',
            'updatedAt': '2020-08-13T17:56:39.516802Z',
            'nominalStartTime': None,
            'nominalEndTime': None,
            'state': 'FAILED',
            'startedAt': '2020-08-13T17:56:39.516802Z',
            'endedAt': None,
            'durationMs': None,
            'args': {}
        }

        response = self.client.mark_job_run_as_failed(run_id=run_id)

        assert str(response['id']) == run_id
        assert str(response['state']) == RunState.FAILED.value

    @mock.patch("marquez_client.MarquezClient._post")
    def test_mark_job_run_as_aborted(self, mock_post):
        run_id = str(uuid.uuid4())

        mock_post.return_value = {
            'id': f'{run_id}',
            'createdAt': '2020-08-12T22:36:50.739951Z',
            'updatedAt': '2020-08-13T17:56:39.516802Z',
            'nominalStartTime': None,
            'nominalEndTime': None,
            'state': 'ABORTED',
            'startedAt': '2020-08-13T17:56:39.516802Z',
            'endedAt': None,
            'durationMs': None,
            'args': {}
        }

        response = self.client.mark_job_run_as_aborted(run_id=run_id)

        assert str(response['id']) == run_id
        assert str(response['state']) == RunState.ABORTED.value


if __name__ == '__main__':
    unittest.main()
