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
from marquez.client import Client
from marquez.constants import NO_CONTENT_RESPONSE, NOT_FOUND
from marquez.utils import APIError, InvalidRequestError
from pytest import fixture


@fixture(scope='class')
def marquez_client():
    return Client(host="localhost",
                  port=8080)


@fixture(scope='function')
@vcr.use_cassette(
    'test/fixtures/vcr/test_namespaces/namespace_for_namespace_tests.yaml')
def namespace(marquez_client):
    ns_name = "ns_for_namespace_test_1"
    owner_name = "ns_owner"
    description = "this is a namespace for testing."

    return marquez_client.create_namespace(ns_name, owner_name, description)


@vcr.use_cassette(
    'test/fixtures/vcr/test_namespaces/test_create_namespace.yaml')
def test_create_namespace(marquez_client):
    ns_name = "ns_name_997"
    owner_name = "some_owner"
    description = "this is a very nice namespace."

    created_ns = marquez_client.create_namespace(
        ns_name, owner_name, description)
    assert created_ns['name'] == ns_name
    assert created_ns['owner'] == owner_name
    assert created_ns['description'] == description


@vcr.use_cassette('test/fixtures/vcr/test_namespaces/test_get_namespace.yaml')
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
    'test/fixtures/vcr/test_namespaces/'
    'test_create_namespace_missing_owner.yaml')
def test_create_namespace_missing_owner(marquez_client):
    missing_namespace_owner = None
    with pytest.raises(InvalidRequestError):
        marquez_client.create_namespace(
            "some_ns_name", missing_namespace_owner)


def test_namespace_internal_service_error(marquez_client, mock_500_response):
    with pytest.raises(APIError):
        marquez_client.get_namespace('any_old_namespace')


def test_namespace_204_no_content_response(marquez_client, mock_204_response):
    assert NO_CONTENT_RESPONSE == marquez_client.get_namespace(
        'any_old_namespace')


def test_namespace_general_error(
        marquez_client, mock_default_exception_response):
    with pytest.raises(Exception):
        marquez_client.get_namespace('any_old_namespace')


@vcr.use_cassette(
    'test/fixtures/vcr/test_namespaces/test_get_no_such_namespace.yaml')
def test_get_no_such_namespace(marquez_client):
    no_such_namespace = "no_such_namespace123"
    assert marquez_client.get_namespace(no_such_namespace) == NOT_FOUND


@vcr.use_cassette(
    'test/fixtures/vcr/test_namespaces/test_list_namespaces.yaml')
def test_list_namespaces(marquez_client, namespace):
    all_namespaces = marquez_client.list_namespaces()
    assert namespace in all_namespaces['namespaces']


@vcr.use_cassette(
    'test/fixtures/vcr/test_namespaces/test_namespaces_not_set.yaml')
def test_namespace_not_set(marquez_client):
    result = marquez_client.create_job('some_job', 'some_location',
                                       ['input1', 'input2'],
                                       ['output1', 'output2'])
    assert result['name'] == 'some_job'


@pytest.fixture()
def mock_204_response(marquez_client):
    original_function = marquez_client.get_request
    response_obj = MagicMock()
    response_obj.text = ''
    response_obj.status_code = HTTPStatus.NO_CONTENT
    marquez_client.get_request = MagicMock(return_value=response_obj)
    yield
    marquez_client.get_request = original_function


@pytest.fixture()
def mock_500_response(marquez_client):
    original_function = marquez_client.get_request
    response_obj = MagicMock()
    response_obj.text = ''
    response_obj.status_code = HTTPStatus.INTERNAL_SERVER_ERROR
    marquez_client.get_request = MagicMock(return_value=response_obj)
    yield
    marquez_client.get_request = original_function


@pytest.fixture()
def mock_default_exception_response(marquez_client):
    original_function = marquez_client.get_request
    response_obj = MagicMock()
    response_obj.text = 'a 508 response. loop detected?!'
    response_obj.status_code = HTTPStatus.LOOP_DETECTED
    marquez_client.get_request = MagicMock(return_value=response_obj)
    yield
    marquez_client.get_request = original_function
