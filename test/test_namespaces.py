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


@fixture(scope='class')
def marquez_client():
    return MarquezClient(host="localhost",
                         port=8080)


@fixture(scope='function')
@vcr.use_cassette(
    'test/fixtures/vcr/test_job/namespace_for_namespace_tests.yaml')
def namespace(marquez_client):
    ns_name = "ns_for_namespace_test_1"
    owner_name = "ns_owner"
    description = "this is a namespace for testing."

    return marquez_client._create_namespace(ns_name, owner_name, description)


@vcr.use_cassette(
    'test/fixtures/vcr/test_namespaces/test_create_namespace.yaml')
def test_create_namespace(marquez_client):
    ns_name = "ns_name_997"
    owner_name = "some_owner"
    description = "this is a very nice namespace."

    marquez_client.set_namespace(ns_name, owner_name, description)
    client_ns = marquez_client.get_namespace()
    assert_str = "mismatch between created namespace and ns set"
    assert client_ns == ns_name, assert_str


@vcr.use_cassette('test/fixtures/vcr/test_namespaces/test_get_namespace.yaml')
def test_get_namespace(marquez_client, namespace):
    returned_ns = marquez_client.get_namespace_info(namespace.name)
    assert returned_ns.name == namespace.name
    assert returned_ns.description == namespace.description
    assert returned_ns.owner == namespace.owner


def test_get_namespace_invalid_input(marquez_client):
    ns_name_none = None
    with pytest.raises(Exception):
        marquez_client.get_namespace_info(ns_name_none)


def test_namespace_not_set(marquez_client):
    with pytest.raises(Exception):
        marquez_client.create_job('some_job', 'some_location',
                                  ['input1', 'input2'],
                                  ['output1', 'output2'])
