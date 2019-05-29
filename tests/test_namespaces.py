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

from http import HTTPStatus
from unittest.mock import MagicMock

import pytest
import vcr
from marquez_client import MarquezClient
from marquez_client import errors
from pytest import fixture


@fixture(scope='class')
def marquez_client():
    return MarquezClient(host="localhost",
                         port=8080)


@fixture(scope='function')
@vcr.use_cassette(
    'tests/fixtures/vcr/test_namespaces/namespace_for_namespace_tests.yaml')
def namespace(marquez_client):
    ns_name = "ns_for_namespace_test_1"
    owner_name = "ns_owner"
    description = "this is a namespace for testing."

    return marquez_client.create_namespace(ns_name, owner_name, description)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_namespaces/test_create_namespace.yaml')
def test_create_namespace(marquez_client):
    ns_name = "ns_name_997"
    owner_name = "some_owner"
    description = "this is a very nice namespace."

    created_ns = marquez_client.create_namespace(
        ns_name, owner_name, description)
    assert created_ns['name'] == ns_name
    assert created_ns['owner'] == owner_name
    assert created_ns['description'] == description


@vcr.use_cassette('tests/fixtures/vcr/test_namespaces/test_get_namespace.yaml')
def test_get_namespace(marquez_client, namespace):
    returned_ns = marquez_client.get_namespace(namespace['name'])
    assert returned_ns['name'] == namespace['name']
    assert returned_ns['description'] == namespace['description']
    assert returned_ns['owner'] == namespace['owner']


def test_get_namespace_invalid_input(marquez_client):
    ns_name_none = None
    with pytest.raises(ValueError):
        marquez_client.get_namespace(ns_name_none)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_namespaces/'
    'test_create_namespace_missing_owner.yaml')
def test_create_namespace_missing_owner(marquez_client):
    missing_namespace_owner = None
    with pytest.raises(ValueError):
        marquez_client.create_namespace(
            "some_ns_name", missing_namespace_owner)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_namespaces/test_get_no_such_namespace.yaml')
def test_get_no_such_namespace(marquez_client):
    no_such_namespace = "no_such_namespace123"
    with pytest.raises(errors.APIError):
        marquez_client.get_namespace(no_such_namespace)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_namespaces/test_list_namespaces.yaml')
def test_list_namespaces(marquez_client, namespace):
    all_namespaces = marquez_client.list_namespaces()
    assert namespace in all_namespaces['namespaces']


@vcr.use_cassette(
    'tests/fixtures/vcr/test_namespaces/test_namespaces_not_set.yaml')
def test_namespace_not_set(marquez_client):
    result = marquez_client.create_job('some_job', 'some_location',
                                       ['input1', 'input2'],
                                       ['output1', 'output2'])
    assert result['name'] == 'some_job'
