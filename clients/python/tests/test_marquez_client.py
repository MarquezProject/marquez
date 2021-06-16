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

import mock
import pytest
import uuid

from http import HTTPStatus
from datetime import timedelta, date

from marquez_client import MarquezClient
from marquez_client.constants import (
    DEFAULT_LIMIT,
    DEFAULT_OFFSET
)
from marquez_client.models import (
    DatasetType,
    DatasetId,
    JobType,
    JobId,
    RunState
)
from marquez_client.utils import Utils

# COMMON
NOW = date.today()
NOW_AS_ISO = date.today().isoformat()
LAST_MODIFIED_AT = date.today().isoformat()
DESCRIPTION = 'test description'
TAG_NAME = 'test'
TAGS = [TAG_NAME]
VERSION = str(uuid.uuid4())

# NAMESPACE
NAMESPACE_NAME = 'test-namespace'
OWNER_NAME = 'test'
NAMESPACE = {
    'name': NAMESPACE_NAME,
    'createdAt': NOW_AS_ISO,
    'updatedAt': NOW_AS_ISO,
    'ownerName': OWNER_NAME,
    'description': DESCRIPTION
}

# SOURCE
SOURCE_TYPE = 'POSTGRESQL'
SOURCE_NAME = 'test-source'
CONNECTION_URL = 'postgresql://localhost:5432/test'
SOURCE = {
    'type': SOURCE_TYPE,
    'name': SOURCE_NAME,
    'createdAt': NOW_AS_ISO,
    'updatedAt': NOW_AS_ISO,
    'connectionUrl': CONNECTION_URL,
    'description': DESCRIPTION
}

# DB TABLE DATASET
DB_TABLE_NAME = 'test.table'
DB_TABLE_ID = DatasetId(
    NAMESPACE_NAME,
    DB_TABLE_NAME
)
DB_TABLE_PHYSICAL_NAME = DB_TABLE_NAME
DB_TABLE_SOURCE_NAME = SOURCE_NAME
FIELDS = [
    {'name': 'field0', 'type': 'INTEGER'},
    {'name': 'field1', 'type': 'INTEGER'},
    {'name': 'field2', 'type': 'STRING'}
]
DB_TABLE = {
    'id': DB_TABLE_ID,
    'type': DatasetType.DB_TABLE,
    'name': DB_TABLE_NAME,
    'physicalName': DB_TABLE_PHYSICAL_NAME,
    'createdAt': NOW_AS_ISO,
    'updatedAt': NOW_AS_ISO,
    'sourceName': SOURCE_NAME,
    'fields': FIELDS,
    'tags': [],
    'lastModifiedAt': None,
    'description': DESCRIPTION
}
FIELDS_MODIFIED = [
    {'name': 'field0', 'type': 'INTEGER', 'tags': TAGS},
    {'name': 'field1', 'type': 'INTEGER'},
    {'name': 'field2', 'type': 'STRING'}
]
DB_TABLE_MODIFIED = {
    'id': DB_TABLE_ID,
    'type': DatasetType.DB_TABLE,
    'name': DB_TABLE_NAME,
    'physicalName': DB_TABLE_PHYSICAL_NAME,
    'createdAt': NOW_AS_ISO,
    'updatedAt': NOW_AS_ISO,
    'namespace': NAMESPACE_NAME,
    'sourceName': SOURCE_NAME,
    'fields': FIELDS_MODIFIED,
    'tags': TAGS,
    'lastModifiedAt': LAST_MODIFIED_AT,
    'description': DESCRIPTION
}

# JOB
JOB_NAME = 'test-job'
JOB_ID = JobId(
    NAMESPACE_NAME,
    JOB_NAME
)
INPUTS = [DB_TABLE_ID]
OUTPUTS = []
LOCATION = 'https://github.com/test-org/test-repo/blob/281fe99/test_job.py'
JOB = {
    'id': JOB_ID,
    'type': JobType.BATCH,
    'name': JOB_NAME,
    'createdAt': NOW_AS_ISO,
    'updatedAt': NOW_AS_ISO,
    'namespace': NAMESPACE_NAME,
    'inputs': INPUTS,
    'outputs': OUTPUTS,
    'location': LOCATION,
    'context': None,
    'description': DESCRIPTION,
    'latestRun': None
}

# RUN
NOMINAL_START_TIME = NOW_AS_ISO
NOMINAL_END_TIME = (NOW + timedelta(days=1)).isoformat()
START_AT = NOW
ENDED_AT = START_AT + timedelta(hours=1)
DURATION_MS = ((ENDED_AT - START_AT).total_seconds() * 1000)
JOB_WITH_LATEST_RUN = {}
RUN_ID = str(uuid.uuid4())
NEW = {
    'id': RUN_ID,
    'createdAt': NOW_AS_ISO,
    'updatedAt': NOW_AS_ISO,
    'nominalStartTime': NOMINAL_START_TIME,
    'nominalEndTime': NOMINAL_END_TIME,
    'state': RunState.NEW,
    'startedAt': None,
    'endedAt': None,
    'durationMs': None,
    'args': None
}
RUNNING = {
    'id': RUN_ID,
    'createdAt': NOW_AS_ISO,
    'updatedAt': NOW_AS_ISO,
    'nominalStartTime': NOMINAL_START_TIME,
    'nominalEndTime': NOMINAL_END_TIME,
    'state': RunState.RUNNING,
    'startedAt': START_AT,
    'endedAt': None,
    'durationMs': None,
    'args': None
}
COMPLETED = {
    'id': RUN_ID,
    'createdAt': NOW_AS_ISO,
    'updatedAt': NOW_AS_ISO,
    'nominalStartTime': NOMINAL_START_TIME,
    'nominalEndTime': NOMINAL_END_TIME,
    'state': RunState.COMPLETED,
    'startedAt': START_AT,
    'endedAt': ENDED_AT,
    'durationMs': DURATION_MS,
    'args': None
}
ABORTED = {
    'id': RUN_ID,
    'createdAt': NOW_AS_ISO,
    'updatedAt': NOW_AS_ISO,
    'nominalStartTime': NOMINAL_START_TIME,
    'nominalEndTime': NOMINAL_END_TIME,
    'state': RunState.ABORTED,
    'startedAt': START_AT,
    'endedAt': ENDED_AT,
    'durationMs': DURATION_MS,
    'args': None
}
FAILED = {
    'id': RUN_ID,
    'createdAt': NOW_AS_ISO,
    'updatedAt': NOW_AS_ISO,
    'nominalStartTime': NOMINAL_START_TIME,
    'nominalEndTime': NOMINAL_END_TIME,
    'state': RunState.FAILED,
    'startedAt': START_AT,
    'endedAt': ENDED_AT,
    'durationMs': DURATION_MS,
    'args': None
}

# DATASET VERSIONS
DB_TABLE_VERSION = {
    'id': DB_TABLE_ID,
    'type': DatasetType.DB_TABLE,
    'name': DB_TABLE_NAME,
    'physicalName': DB_TABLE_PHYSICAL_NAME,
    'createdAt': NOW_AS_ISO,
    'version': VERSION,
    'namespace': NAMESPACE_NAME,
    'sourceName': SOURCE_NAME,
    'fields': FIELDS,
    'tags': [],
    'description': DESCRIPTION,
    'createdByRun': COMPLETED
}

# TAGS
TAG_NAME2 = "second_tag"
TAG_DESCRIPTION2 = "second tag description"
TAG = {
    'name': TAG_NAME,
    'description': DESCRIPTION
}
TAG_LIST = {
    'tags': [{TAG_NAME: DESCRIPTION}, {TAG_NAME2: TAG_DESCRIPTION2}]
}


@pytest.fixture
def client():
    return MarquezClient(url='http;//localhost:5000')


@mock.patch('requests.put')
def test_create_namespace(mock_put, client):
    mock_put.return_value.status_code.return_value = HTTPStatus.OK
    mock_put.return_value.json.return_value = NAMESPACE

    namespace = client.create_namespace(
        NAMESPACE_NAME,
        OWNER_NAME,
        DESCRIPTION
    )

    assert namespace['name'] == NAMESPACE_NAME
    assert namespace['ownerName'] == OWNER_NAME
    assert namespace['description'] == DESCRIPTION

    mock_put.assert_called_once_with(
        url=client._url('/namespaces/{0}', NAMESPACE_NAME),
        headers=mock.ANY,
        json={
            'ownerName': OWNER_NAME,
            'description': DESCRIPTION
        },
        timeout=mock.ANY
    )


@mock.patch('requests.get')
def test_get_namespace(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = NAMESPACE

    namespace = client.get_namespace(NAMESPACE_NAME)

    assert namespace['name'] == NAMESPACE_NAME
    assert namespace['ownerName'] == OWNER_NAME
    assert namespace['description'] == DESCRIPTION

    mock_get.assert_called_once_with(
        url=client._url('/namespaces/{0}', NAMESPACE_NAME),
        params=mock.ANY,
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch('requests.get')
def test_list_namespaces(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = [NAMESPACE]

    namespaces = client.list_namespaces()

    assert len(namespaces) == 1
    assert namespaces[0]['name'] == NAMESPACE_NAME
    assert namespaces[0]['ownerName'] == OWNER_NAME
    assert namespaces[0]['description'] == DESCRIPTION

    mock_get.assert_called_once_with(
        url=client._url('/namespaces'),
        params={
            'limit': DEFAULT_LIMIT,
            'offset': DEFAULT_OFFSET
        },
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch("requests.put")
def test_create_source(mock_put, client):
    mock_put.return_value.status_code.return_value = HTTPStatus.OK
    mock_put.return_value.json.return_value = SOURCE

    source = client.create_source(
        SOURCE_NAME,
        SOURCE_TYPE,
        CONNECTION_URL,
        DESCRIPTION
    )

    assert source['type'] == SOURCE_TYPE
    assert source['name'] == SOURCE_NAME
    assert source['connectionUrl'] == CONNECTION_URL
    assert source['description'] == DESCRIPTION

    mock_put.assert_called_once_with(
        url=client._url('/sources/{0}', SOURCE_NAME),
        headers=mock.ANY,
        json={
            'type': SOURCE_TYPE,
            'connectionUrl': CONNECTION_URL,
            'description': DESCRIPTION
        },
        timeout=mock.ANY
    )


@mock.patch("requests.get")
def test_get_source(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = SOURCE

    source = client.get_source(SOURCE_NAME)

    assert source['type'] == SOURCE_TYPE
    assert source['name'] == SOURCE_NAME
    assert source['connectionUrl'] == CONNECTION_URL
    assert source['description'] == DESCRIPTION

    mock_get.assert_called_once_with(
        url=client._url('/sources/{0}', SOURCE_NAME),
        params=mock.ANY,
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch("requests.get")
def test_list_sources(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = [SOURCE]

    sources = client.list_sources()

    assert len(sources) == 1
    assert sources[0]['type'] == SOURCE_TYPE
    assert sources[0]['name'] == SOURCE_NAME
    assert sources[0]['connectionUrl'] == CONNECTION_URL
    assert sources[0]['description'] == DESCRIPTION

    mock_get.assert_called_once_with(
        url=client._url('/sources'),
        params={
            'limit': DEFAULT_LIMIT,
            'offset': DEFAULT_OFFSET
        },
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch('requests.put')
def test_create_dataset(mock_put, client):
    mock_put.return_value.status_code.return_value = HTTPStatus.OK
    mock_put.return_value.json.return_value = DB_TABLE

    dataset = client.create_dataset(
        namespace_name=NAMESPACE_NAME,
        dataset_type=DatasetType.DB_TABLE,
        dataset_name=DB_TABLE_NAME,
        dataset_physical_name=DB_TABLE_PHYSICAL_NAME,
        source_name=SOURCE_NAME,
        fields=FIELDS,
        description=DESCRIPTION
    )

    assert dataset['id'] == DB_TABLE_ID
    assert dataset['type'] == DatasetType.DB_TABLE
    assert dataset['name'] == DB_TABLE_NAME
    assert dataset['physicalName'] == DB_TABLE_PHYSICAL_NAME
    assert dataset['sourceName'] == SOURCE_NAME
    assert dataset['fields'] == FIELDS
    assert dataset['description'] == DESCRIPTION

    mock_put.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/datasets/{1}', NAMESPACE_NAME, DB_TABLE_NAME
        ),
        headers=mock.ANY,
        json={
            'type': DatasetType.DB_TABLE.value,
            'physicalName': DB_TABLE_PHYSICAL_NAME,
            'sourceName': SOURCE_NAME,
            'fields': FIELDS,
            'description': DESCRIPTION
        },
        timeout=mock.ANY
    )


@mock.patch('requests.get')
def test_get_dataset(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = DB_TABLE

    dataset = client.get_dataset(
        namespace_name=NAMESPACE_NAME,
        dataset_name=DB_TABLE_NAME
    )

    assert dataset['id'] == DB_TABLE_ID
    assert dataset['type'] == DatasetType.DB_TABLE
    assert dataset['name'] == DB_TABLE_NAME
    assert dataset['physicalName'] == DB_TABLE_PHYSICAL_NAME
    assert dataset['sourceName'] == SOURCE_NAME
    assert dataset['fields'] == FIELDS
    assert dataset['description'] == DESCRIPTION

    mock_get.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/datasets/{1}', NAMESPACE_NAME, DB_TABLE_NAME
        ),
        params=mock.ANY,
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch('requests.get')
def test_get_dataset_version(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = DB_TABLE_VERSION

    dataset_version = client.get_dataset_version(
        namespace_name=NAMESPACE_NAME,
        dataset_name=DB_TABLE_NAME,
        version=VERSION
    )

    assert dataset_version['id'] == DB_TABLE_ID
    assert dataset_version['type'] == DatasetType.DB_TABLE
    assert dataset_version['name'] == DB_TABLE_NAME
    assert dataset_version['physicalName'] == DB_TABLE_PHYSICAL_NAME
    assert dataset_version['version'] == VERSION
    assert dataset_version['sourceName'] == SOURCE_NAME
    assert dataset_version['fields'] == FIELDS
    assert dataset_version['description'] == DESCRIPTION
    assert dataset_version['createdByRun'] == COMPLETED

    mock_get.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/datasets/{1}/versions/{2}',
            NAMESPACE_NAME, DB_TABLE_NAME, VERSION
        ),
        params=mock.ANY,
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch('requests.get')
def test_list_dataset_versions(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = [DB_TABLE_VERSION]

    dataset_versions = client.list_dataset_versions(
        NAMESPACE_NAME, DB_TABLE_NAME
    )

    assert dataset_versions[0]['id'] == DB_TABLE_ID
    assert dataset_versions[0]['type'] == DatasetType.DB_TABLE
    assert dataset_versions[0]['name'] == DB_TABLE_NAME
    assert dataset_versions[0]['physicalName'] == DB_TABLE_PHYSICAL_NAME
    assert dataset_versions[0]['version'] == VERSION
    assert dataset_versions[0]['sourceName'] == SOURCE_NAME
    assert dataset_versions[0]['fields'] == FIELDS
    assert dataset_versions[0]['description'] == DESCRIPTION
    assert dataset_versions[0]['createdByRun'] == COMPLETED

    mock_get.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/datasets/{1}/versions',
            NAMESPACE_NAME, DB_TABLE_NAME
        ),
        params={
            'limit': DEFAULT_LIMIT,
            'offset': DEFAULT_OFFSET
        },
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch('requests.get')
def test_list_datasets(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = [DB_TABLE]

    datasets = client.list_datasets(NAMESPACE_NAME)

    assert datasets[0]['id'] == DB_TABLE_ID
    assert datasets[0]['type'] == DatasetType.DB_TABLE
    assert datasets[0]['name'] == DB_TABLE_NAME
    assert datasets[0]['physicalName'] == DB_TABLE_PHYSICAL_NAME
    assert datasets[0]['sourceName'] == SOURCE_NAME
    assert datasets[0]['fields'] == FIELDS
    assert datasets[0]['description'] == DESCRIPTION

    mock_get.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/datasets', NAMESPACE_NAME
        ),
        params={
            'limit': DEFAULT_LIMIT,
            'offset': DEFAULT_OFFSET
        },
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch('requests.post')
def test_tag_dataset(mock_post, client):
    mock_post.return_value.status_code.return_value = HTTPStatus.OK
    mock_post.return_value.json.return_value = DB_TABLE_MODIFIED

    modified_dataset = client.tag_dataset(
        namespace_name=NAMESPACE_NAME,
        dataset_name=DB_TABLE_NAME,
        tag_name=TAG_NAME
    )

    assert modified_dataset['id'] == DB_TABLE_ID
    assert modified_dataset['type'] == DatasetType.DB_TABLE
    assert modified_dataset['name'] == DB_TABLE_NAME
    assert modified_dataset['physicalName'] == DB_TABLE_PHYSICAL_NAME
    assert modified_dataset['sourceName'] == SOURCE_NAME
    assert modified_dataset['fields'] == FIELDS_MODIFIED
    assert modified_dataset['tags'] == TAGS
    assert modified_dataset['lastModifiedAt'] == LAST_MODIFIED_AT
    assert modified_dataset['description'] == DESCRIPTION

    mock_post.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/datasets/{1}/tags/{2}',
            NAMESPACE_NAME, DB_TABLE_NAME, TAG_NAME
        ),
        headers=mock.ANY,
        json=None,
        timeout=mock.ANY
    )


@mock.patch('requests.post')
def test_tag_dataset_field(mock_post, client):
    mock_post.return_value.status_code.return_value = HTTPStatus.OK
    mock_post.return_value.json.return_value = DB_TABLE_MODIFIED

    modified_dataset = client.tag_dataset_field(
        namespace_name=NAMESPACE_NAME,
        dataset_name=DB_TABLE_NAME,
        dataset_field_name='field0',
        tag_name=TAG_NAME
    )

    assert modified_dataset['id'] == DB_TABLE_ID
    assert modified_dataset['type'] == DatasetType.DB_TABLE
    assert modified_dataset['name'] == DB_TABLE_NAME
    assert modified_dataset['physicalName'] == DB_TABLE_PHYSICAL_NAME
    assert modified_dataset['sourceName'] == SOURCE_NAME
    assert modified_dataset['fields'] == FIELDS_MODIFIED
    assert modified_dataset['tags'] == TAGS
    assert modified_dataset['lastModifiedAt'] == LAST_MODIFIED_AT
    assert modified_dataset['description'] == DESCRIPTION

    mock_post.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/datasets/{1}/fields/{2}/tags/{3}',
            NAMESPACE_NAME, DB_TABLE_NAME, 'field0', TAG_NAME
        ),
        headers=mock.ANY,
        json=None,
        timeout=mock.ANY
    )


@mock.patch('requests.put')
def test_create_job(mock_put, client):
    mock_put.return_value.status_code.return_value = HTTPStatus.OK
    mock_put.return_value.json.return_value = JOB

    job = client.create_job(
        namespace_name=NAMESPACE_NAME,
        job_type=JobType.BATCH,
        job_name=JOB_NAME,
        inputs=INPUTS,
        outputs=OUTPUTS,
        location=LOCATION,
        description=DESCRIPTION
    )

    assert job['id'] == JOB_ID
    assert job['type'] == JobType.BATCH
    assert job['name'] == JOB_NAME
    assert job['inputs'] == INPUTS
    assert job['outputs'] == OUTPUTS
    assert job['location'] == LOCATION
    assert job['description'] == DESCRIPTION

    mock_put.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/jobs/{1}', NAMESPACE_NAME, JOB_NAME
        ),
        headers=mock.ANY,
        json={
            'type': JobType.BATCH.value,
            'inputs': [
                input.__dict__ for input in INPUTS
            ],
            'outputs': [
                output.__dict__ for output in OUTPUTS
            ],
            'location': LOCATION,
            'description': DESCRIPTION
        },
        timeout=mock.ANY
    )


@mock.patch('requests.get')
def test_get_job(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = JOB

    job = client.get_job(
        namespace_name=NAMESPACE_NAME,
        job_name=JOB_NAME
    )

    assert job['id'] == JOB_ID
    assert job['type'] == JobType.BATCH
    assert job['name'] == JOB_NAME
    assert job['inputs'] == INPUTS
    assert job['outputs'] == OUTPUTS
    assert job['location'] == LOCATION
    assert job['description'] == DESCRIPTION

    mock_get.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/jobs/{1}', NAMESPACE_NAME, JOB_NAME
        ),
        params=mock.ANY,
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch('requests.get')
def test_list_jobs(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = JOB

    job = client.list_jobs(NAMESPACE_NAME)

    assert job['id'] == JOB_ID
    assert job['type'] == JobType.BATCH
    assert job['name'] == JOB_NAME
    assert job['inputs'] == INPUTS
    assert job['outputs'] == OUTPUTS
    assert job['location'] == LOCATION
    assert job['description'] == DESCRIPTION

    mock_get.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/jobs', NAMESPACE_NAME
        ),
        params={
            'limit': DEFAULT_LIMIT,
            'offset': DEFAULT_OFFSET
        },
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch('requests.post')
def test_create_job_run(mock_post, client):
    mock_post.return_value.status_code.return_value = HTTPStatus.OK
    mock_post.return_value.json.return_value = NEW

    run = client.create_job_run(
        namespace_name=NAMESPACE_NAME,
        job_name=JOB_NAME,
        nominal_start_time=NOMINAL_START_TIME,
        nominal_end_time=NOMINAL_END_TIME
    )

    assert run['id'] == RUN_ID
    assert run['nominalStartTime'] == NOMINAL_START_TIME
    assert run['nominalEndTime'] == NOMINAL_END_TIME
    assert run['state'] == RunState.NEW
    assert run['startedAt'] is None
    assert run['endedAt'] is None
    assert run['durationMs'] is None

    mock_post.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/jobs/{1}/runs', NAMESPACE_NAME, JOB_NAME
        ),
        headers=mock.ANY,
        json={
            'nominalStartTime': NOMINAL_START_TIME,
            'nominalEndTime': NOMINAL_END_TIME,
        },
        timeout=mock.ANY
    )


@mock.patch('requests.get')
def test_list_job_runs(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = [NEW]

    runs = client.list_job_runs(
        namespace_name=NAMESPACE_NAME,
        job_name=JOB_NAME
    )

    assert runs[0]['id'] == RUN_ID
    assert runs[0]['nominalStartTime'] == NOMINAL_START_TIME
    assert runs[0]['nominalEndTime'] == NOMINAL_END_TIME
    assert runs[0]['state'] == RunState.NEW
    assert runs[0]['startedAt'] is None
    assert runs[0]['endedAt'] is None
    assert runs[0]['durationMs'] is None

    mock_get.assert_called_once_with(
        url=client._url(
            '/namespaces/{0}/jobs/{1}/runs',
            NAMESPACE_NAME, JOB_NAME
        ),
        params={
            'limit': DEFAULT_LIMIT,
            'offset': DEFAULT_OFFSET
        },
        headers=mock.ANY,
        timeout=mock.ANY
    )


@mock.patch('requests.post')
def test_mark_job_run_as_started(mock_post, client):
    mock_post.return_value.status_code.return_value = HTTPStatus.OK
    mock_post.return_value.json.return_value = RUNNING

    now = Utils.utc_now()
    run = client.mark_job_run_as_started(RUN_ID, at=now)

    assert run['id'] == RUN_ID
    assert run['nominalStartTime'] == NOMINAL_START_TIME
    assert run['nominalEndTime'] == NOMINAL_END_TIME
    assert run['state'] == RunState.RUNNING
    assert run['startedAt'] == START_AT
    assert run['endedAt'] is None
    assert run['durationMs'] is None

    mock_post.assert_called_once_with(
        url=client._url(
            '/jobs/runs/{0}/start?at={1}', RUN_ID, now
        ),
        headers=mock.ANY,
        json=None,
        timeout=mock.ANY
    )


@mock.patch('requests.post')
def test_mark_job_run_as_completed(mock_post, client):
    mock_post.return_value.status_code.return_value = 200
    mock_post.return_value.json.return_value = COMPLETED

    now = Utils.utc_now()
    run = client.mark_job_run_as_completed(RUN_ID, at=now)

    assert run['id'] == RUN_ID
    assert run['nominalStartTime'] == NOMINAL_START_TIME
    assert run['nominalEndTime'] == NOMINAL_END_TIME
    assert run['state'] == RunState.COMPLETED
    assert run['startedAt'] == START_AT
    assert run['endedAt'] == ENDED_AT
    assert run['durationMs'] == DURATION_MS

    mock_post.assert_called_once_with(
        url=client._url(
            '/jobs/runs/{0}/complete?at={1}', RUN_ID, now
        ),
        headers=mock.ANY,
        json=None,
        timeout=mock.ANY
    )


@mock.patch('requests.post')
def test_mark_job_run_as_aborted(mock_post, client):
    mock_post.return_value.status_code.return_value = HTTPStatus.OK
    mock_post.return_value.json.return_value = ABORTED

    now = Utils.utc_now()
    run = client.mark_job_run_as_aborted(RUN_ID, at=now)

    assert run['id'] == RUN_ID
    assert run['nominalStartTime'] == NOMINAL_START_TIME
    assert run['nominalEndTime'] == NOMINAL_END_TIME
    assert run['state'] == RunState.ABORTED
    assert run['startedAt'] == START_AT
    assert run['endedAt'] == ENDED_AT
    assert run['durationMs'] == DURATION_MS

    mock_post.assert_called_once_with(
        url=client._url(
            '/jobs/runs/{0}/abort?at={1}', RUN_ID, now
        ),
        headers=mock.ANY,
        json=None,
        timeout=mock.ANY
    )


@mock.patch('requests.post')
def test_mark_job_run_as_failed(mock_post, client):
    mock_post.return_value.status_code.return_value = HTTPStatus.OK
    mock_post.return_value.json.return_value = FAILED

    now = Utils.utc_now()
    run = client.mark_job_run_as_failed(RUN_ID, at=now)

    assert run['id'] == RUN_ID
    assert run['nominalStartTime'] == NOMINAL_START_TIME
    assert run['nominalEndTime'] == NOMINAL_END_TIME
    assert run['state'] == RunState.FAILED
    assert run['startedAt'] == START_AT
    assert run['endedAt'] == ENDED_AT
    assert run['durationMs'] == DURATION_MS

    mock_post.assert_called_once_with(
        url=client._url(
            '/jobs/runs/{0}/fail?at={1}', RUN_ID, now
        ),
        headers=mock.ANY,
        json=None,
        timeout=mock.ANY
    )


@mock.patch('requests.put')
def test_create_tag(mock_put, client):
    mock_put.return_value.status_code.return_value = HTTPStatus.OK
    mock_put.return_value.json.return_value = TAG

    tag = client.create_tag(TAG_NAME, DESCRIPTION)

    assert tag['name'] == TAG_NAME
    assert tag['description'] == DESCRIPTION

    mock_put.assert_called_once_with(
        url=client._url(
            '/tags/{0}', TAG_NAME
        ),
        headers=mock.ANY,
        json={
            "description": DESCRIPTION
        },
        timeout=mock.ANY
    )


@mock.patch('requests.get')
def test_list_tags(mock_get, client):
    mock_get.return_value.status_code.return_value = HTTPStatus.OK
    mock_get.return_value.json.return_value = TAG_LIST

    tags = client.list_tags(limit=DEFAULT_LIMIT, offset=DEFAULT_OFFSET)

    assert tags == TAG_LIST

    mock_get.assert_called_once_with(
        url=client._url(
            '/tags'
        ),
        headers=mock.ANY,
        params={
            'limit': DEFAULT_LIMIT,
            'offset': DEFAULT_OFFSET
        },
        timeout=mock.ANY
    )
