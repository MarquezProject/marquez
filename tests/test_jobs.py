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
import vcr
from marquez_client import MarquezClient
from marquez_client import errors
import pytest
from pytest import fixture


@fixture(scope='class')
def namespace_name():
    return "default"


@fixture(scope='class')
@vcr.use_cassette('tests/fixtures/vcr/test_jobs/namespace_for_job.yaml')
def namespace(namespace_name):
    owner_name = "some_owner"
    description = "this is a very nice namespace."
    basic_marquez_client = MarquezClient(host="localhost", port=5000)
    created_ns = basic_marquez_client.create_namespace(
        namespace_name, owner_name, description)
    return created_ns


@fixture(scope='class')
def marquez_client_with_ns(namespace_name):
    return MarquezClient(host="localhost", namespace_name=namespace_name,
                         port=5000)


@fixture(scope='class')
def marquez_client_with_default_ns():
    return MarquezClient(host="localhost", port=5000)


@fixture(scope='class')
def job_name():
    return 'job_fixture'


@fixture(scope='class')
@vcr.use_cassette('tests/fixtures/vcr/test_jobs/job_for_jobs_test.yaml')
def job(marquez_client_with_ns, job_name):
    input_datsets = ['input1a', 'input2a']
    output_datsets = ['output1a', 'output2a']
    context = {'sql': "SELECT * FROM room_bookings WHERE room_num = '2H';"}
    return marquez_client_with_ns.create_job(
        job_name, 'BATCH', 'https://github.com/wework/jobs/commit/124f',
        input_datsets,
        output_datsets,
        context=context)


@fixture(scope='class')
@vcr.use_cassette(
    'tests/fixtures/vcr/test_jobs/job_default_ns_for_jobs_test.yaml')
def job_default_ns(job_name):
    marquez_client = MarquezClient(host="localhost", port=5000)

    input_datsets = ['input1a', 'input2a']
    output_datsets = ['output1a', 'output2a']
    return marquez_client.create_job(
        job_name, 'BATCH', 'https://github.com/wework/jobs/commit/124f',
        input_datsets,
        output_datsets)


@vcr.use_cassette('tests/fixtures/vcr/test_jobs/test_create_job.yaml')
def test_create_job(marquez_client_with_default_ns):
    _run_job_creation_test('some_job4', marquez_client_with_default_ns)


@vcr.use_cassette('tests/fixtures/vcr/test_jobs/test_get_job.yaml')
def test_get_job(marquez_client_with_ns, job):
    retrieved_job = marquez_client_with_ns.get_job(job['name'])

    assert retrieved_job['location'] == job['location']
    assert retrieved_job['name'] == job['name']
    assert retrieved_job['inputs'] == job['inputs']
    assert retrieved_job['outputs'] == job['outputs']
    assert 'createdAt' in retrieved_job


@vcr.use_cassette('tests/fixtures/vcr/test_jobs/test_list_jobs.yaml')
def test_list_jobs(marquez_client_with_default_ns, job_default_ns):
    retrieved_jobs = marquez_client_with_default_ns.list_jobs()
    assert job_default_ns in retrieved_jobs['jobs']


@vcr.use_cassette('tests/fixtures/vcr/test_jobs/test_list_jobs_with_ns.yaml')
def test_list_jobs_with_namespace_client(marquez_client_with_ns, job):
    retrieved_jobs = marquez_client_with_ns.list_jobs()
    assert job in retrieved_jobs['jobs']


@vcr.use_cassette('tests/fixtures/vcr/test_jobs/test_list_jobs.yaml')
def test_list_jobs_specify_namespace(
        marquez_client_with_default_ns, job_default_ns):
    retrieved_jobs = marquez_client_with_default_ns.list_jobs()
    assert job_default_ns in retrieved_jobs['jobs']


@vcr.use_cassette(
    'tests/fixtures/vcr/test_jobs/test_list_jobs_no_such_namespace.yaml')
def test_list_jobs_no_such_namespace(marquez_client_with_default_ns):
    no_such_namespace = "no_such_namespace_999"
    with pytest.raises(errors.APIError):
        marquez_client_with_default_ns.list_jobs(
            namespace_name=no_such_namespace)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_jobs/'
    'test_create_job_with_special_chars_no_spaces.yaml')
def test_create_job_with_special_chars_no_spaces(
        marquez_client_with_default_ns):
    job_name = '@ntagon!st\\icj0bname2`'
    _run_job_creation_test(job_name, marquez_client_with_default_ns)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_jobs/'
    'test_create_job_with_forward_slash.yaml')
def test_create_job_with_forward_slash(marquez_client_with_default_ns):
    job_name = 'some/job'
    _run_job_creation_test(job_name, marquez_client_with_default_ns)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_jobs/'
    'test_create_job_with_forward_slashes.yaml')
def test_create_job_with_forward_slashes(marquez_client_with_default_ns):
    job_name = 'some/job/with/more/than/one/slash'
    _run_job_creation_test(job_name, marquez_client_with_default_ns)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_jobs/'
    'test_create_job_with_forward_slashes_and_space.yaml')
def test_create_job_with_forward_slashes_and_space(
        marquez_client_with_default_ns):
    job_name = 'some/job/with/more/than/one/slash 2'
    _run_job_creation_test(job_name, marquez_client_with_default_ns)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_jobs/'
    'test_create_job_with_special_chars_with_spaces.yaml')
def test_create_job_with_special_chars_with_spaces(
        marquez_client_with_default_ns):
    job_name = '@ntago  n!st\\icj0bname2`'
    _run_job_creation_test(job_name, marquez_client_with_default_ns)


def _run_job_creation_test(job_name, marquez_client):
    job_name = job_name
    location = "https://github.com/wework/jobs/commit/124f"
    description = "someDescription"
    input_datsets = ['input1', 'input2']
    output_datsets = ['output1', 'output2']
    created_job = marquez_client.create_job(
        job_name, 'BATCH', location,
        input_datsets, output_datsets,
        description=description)

    assert created_job['location'] == location
    assert created_job['name'] == job_name
    # assert created_job['inputs'] == input_datsets
    # todo: provide valid  inputs
    # assert created_job['outputs'] == output_datsets
    # todo: provide valid outputs
    assert created_job['description'] == description
