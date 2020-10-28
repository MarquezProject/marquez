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

import pytest
import mock

from marquez_client import MarquezWriteOnlyClient
from marquez_client.models import (DatasetType, JobType)
from marquez_client.utils import Utils


@pytest.fixture
def wo_client():
    return MarquezWriteOnlyClient(backend=mock.Mock())


def test_create_namespace(wo_client):
    wo_client.create_namespace(
        namespace_name='test-namespace',
        owner_name='test-owner'
    )

    wo_client._backend.put.assert_called_once_with(
        path='/namespaces/test-namespace',
        headers=mock.ANY,
        payload={'ownerName': 'test-owner'}
    )


def test_create_source(wo_client):
    wo_client.create_source(
        source_name='test-source',
        source_type='postgresql',
        connection_url='postgresql://localhost:5432/testdb'
    )

    wo_client._backend.put.assert_called_once_with(
        path='/sources/test-source',
        headers=mock.ANY,
        payload={
            'type': 'POSTGRESQL',
            'connectionUrl':
                'postgresql://localhost:5432/testdb'
        }
    )


def test_create_dataset(wo_client):
    wo_client.create_dataset(
        namespace_name='test-namespace',
        dataset_name='test-dataset',
        dataset_type=DatasetType.DB_TABLE,
        physical_name='test-physical-dataset',
        source_name='test-source',
        fields=[
            {'name': 'field0', 'type': 'int4'},
            {'name': 'field1', 'type': 'varchar'},
            {'name': 'field2', 'type': 'varchar'},
        ]
    )

    wo_client._backend.put.assert_called_once_with(
        path='/namespaces/test-namespace/datasets/test-dataset',
        headers=mock.ANY,
        payload={
            'type': 'DB_TABLE',
            'physicalName': 'test-physical-dataset',
            'sourceName': 'test-source',
            'fields': [
                {'name': 'field0', 'type': 'INT4'},
                {'name': 'field1', 'type': 'VARCHAR'},
                {'name': 'field2', 'type': 'VARCHAR'}
            ]
        }
    )


def test_create_job(wo_client):
    wo_client.create_job(
        namespace_name='test-namespace',
        job_name='test-job',
        job_type=JobType.BATCH,
        input_dataset=[{
            'namespace': 'test-namespace',
            'name': 'public.test_table'
        }],
        output_dataset=[],
        location=('https://github.com/repo/test/commit/' +
                  '3c34a757fb501f4ee0e297a6ce2888136e495c70'),
        context={
            'sql': 'SELECT * FROM test_table;'
        }
    )

    wo_client._backend.put.assert_called_once_with(
        path='/namespaces/test-namespace/jobs/test-job',
        headers=mock.ANY,
        payload={
            'inputs': [{
                'namespace': 'test-namespace',
                'name': 'public.test_table'
            }],
            'outputs': [],
            'type': 'BATCH',
            'context': {
                'sql': 'SELECT * FROM test_table;'
            },
            'location': ('https://github.com/repo/test/commit/' +
                         '3c34a757fb501f4ee0e297a6ce2888136e495c70')
        }
    )


def test_create_job_run(wo_client):
    wo_client.create_job_run(
        namespace_name='test-namespace',
        job_name='test-job',
        run_id='71feb41b-be50-428c-8470-37b9c292f787'
    )

    wo_client._backend.post.assert_called_once_with(
        path='/namespaces/test-namespace/jobs/test-job/runs',
        headers=mock.ANY,
        payload={
            'id': '71feb41b-be50-428c-8470-37b9c292f787'
        }
    )


def test_mark_job_run_as_started(wo_client):
    started_at = Utils.utc_now()

    wo_client.mark_job_run_as_started(
        run_id='71feb41b-be50-428c-8470-37b9c292f787',
        at=started_at
    )

    wo_client._backend.post.assert_called_once_with(
        path=MarquezWriteOnlyClient._path(
            '/jobs/runs/71feb41b-be50-428c-8470-37b9c292f787/start?at={0}',
            started_at
        ),
        headers=mock.ANY
    )


def test_mark_job_run_as_completed(wo_client):
    completed_at = Utils.utc_now()

    wo_client.mark_job_run_as_completed(
        run_id='71feb41b-be50-428c-8470-37b9c292f787',
        at=completed_at
    )

    wo_client._backend.post.assert_called_once_with(
        path=MarquezWriteOnlyClient._path(
            '/jobs/runs/71feb41b-be50-428c-8470-37b9c292f787/complete?at={0}',
            completed_at
        ),
        headers=mock.ANY
    )


def test_mark_job_run_as_failed(wo_client):
    failed_at = Utils.utc_now()

    wo_client.mark_job_run_as_failed(
        run_id='71feb41b-be50-428c-8470-37b9c292f787',
        at=failed_at
    )

    wo_client._backend.post.assert_called_once_with(
        path=MarquezWriteOnlyClient._path(
            '/jobs/runs/71feb41b-be50-428c-8470-37b9c292f787/fail?at={0}',
            failed_at
        ),
        headers=mock.ANY
    )


def test_mark_job_run_as_aborted(wo_client):
    aborted_at = Utils.utc_now()

    wo_client.mark_job_run_as_aborted(
        run_id='71feb41b-be50-428c-8470-37b9c292f787',
        at=aborted_at
    )

    wo_client._backend.post.assert_called_once_with(
        path=MarquezWriteOnlyClient._path(
            '/jobs/runs/71feb41b-be50-428c-8470-37b9c292f787/abort?at={0}',
            aborted_at
        ),
        headers=mock.ANY
    )


def test_path():
    path = MarquezWriteOnlyClient._path('/namespaces/{0}', 'test-namespace')
    assert path == '/namespaces/test-namespace'
