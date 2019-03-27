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
import vcr
from marquez_client.marquez import MarquezClient
from pytest import fixture


@fixture(scope='function')
def marquez_client():
    return MarquezClient(host="localhost",
                         port=8080)


@fixture(scope='function')
@vcr.use_cassette('test/fixtures/vcr/test_job/namespace_for_job.yaml')
def namespace(marquez_client):
    ns_name = "ns_for_job_test_1"
    owner_name = "some_owner"
    description = "this is a very nice namespace."

    marquez_client.set_namespace(ns_name, owner_name, description)
    client_ns = marquez_client.get_namespace()
    return client_ns


@vcr.use_cassette('test/fixtures/vcr/test_job/test_create_job.yaml')
def test_create_job(marquez_client, namespace):
    marquez_client.set_namespace(namespace)
    created_job = marquez_client.create_job(
        'some_job', 'some_location',
        ['input1', 'input2'],
        ['output1', 'output2'])

    assert created_job.location == 'some_location'
    assert created_job.name == "some_job"


def test_namespace_not_set(marquez_client):
    with pytest.raises(Exception):
        marquez_client.create_job(
            'some_job', 'some_location',
            ['input1', 'input2'],
            ['output1', 'output2'])
