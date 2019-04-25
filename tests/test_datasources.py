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
from marquez_client.client import Client
from marquez_client.utils import InvalidRequestError
from pytest import fixture


@fixture(scope='class')
def marquez_client():
    return Client(host="localhost",
                  port=8080)


@fixture(scope='class')
def marquez_client_with_timeout():
    return Client(host="localhost",
                  port=8080, timeout_ms=4000)


@fixture(scope='class')
@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/datasource_for_datasource_tests.yaml')
def existing_datasource(marquez_client):
    datasource_name = "financials_db200"
    datasource_url = "jdbc:postgresql://localhost:5431/reporting_system"
    return marquez_client.create_datasource(datasource_name, datasource_url)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/test_create_datasource.yaml')
def test_create_datasource(marquez_client):
    datasource_name = "financials_db201"
    datasource_url = "jdbc:postgresql://localhost:5431/reporting_system"
    datasource_response = marquez_client.create_datasource(
        datasource_name, datasource_url)
    assert datasource_response['name'] == datasource_name
    assert datasource_response['connectionUrl'] == datasource_url
    assert 'createdAt' in datasource_response
    assert datasource_response['createdAt'] is not None


@pytest.mark.skip("Disabled until Marquez issue 458 is resolved")
@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/'
    'test_create_datasource_special_chars.yaml')
def test_create_datasource_special_chars(marquez_client):
    datasource_name = "financi@ls db20!"
    datasource_url = "jdbc:postgresql://localhost:5431/reporting_system"
    with pytest.raises(InvalidRequestError):
        marquez_client.create_datasource(datasource_name, datasource_url)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/'
    'test_create_datasource.yaml')
def test_create_datasource_with_timeout(marquez_client):
    datasource_name = "financials_db201"
    datasource_url = "jdbc:postgresql://localhost:5431/reporting_system"
    datasource_response = marquez_client.create_datasource(
        datasource_name, datasource_url)
    assert datasource_response['name'] == datasource_name
    assert datasource_response['connectionUrl'] == datasource_url
    assert 'createdAt' in datasource_response
    assert datasource_response['createdAt'] is not None


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/test_get_datasource.yaml')
def test_get_datasource(marquez_client, existing_datasource):
    response = marquez_client.get_datasource(
        existing_datasource['urn'])
    assert existing_datasource['name'] == response['name']
    assert existing_datasource['urn'] == response['urn']
    response_url = response['connectionUrl']
    assert response_url == existing_datasource['connectionUrl']
    assert response['createdAt'] == existing_datasource['createdAt']


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/test_get_datasources.yaml')
def test_get_datasources(marquez_client, existing_datasource):
    get_datasources_response = marquez_client.list_datasources()
    assert existing_datasource in get_datasources_response['datasources']


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/'
    'test_get_datasources_with_timeout.yaml')
def test_get_datasources_with_timeout(
        marquez_client_with_timeout, existing_datasource):
    get_datasources_response = marquez_client_with_timeout.list_datasources()
    assert existing_datasource in get_datasources_response['datasources']


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/'
    'test_get_datasources_with_limit.yaml')
def test_get_datasources_with_limit(
        marquez_client, existing_datasource):
    get_datasources_response = marquez_client.list_datasources(
        25)
    assert existing_datasource in get_datasources_response['datasources']


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/'
    'test_get_datasources_with_offset.yaml')
def test_get_datasources_with_offset(
        marquez_client, existing_datasource):
    get_datasources_response = marquez_client.list_datasources(
        offset=3)
    assert existing_datasource in get_datasources_response['datasources']


def test_get_namespace_with_invalid_dictionary(marquez_client):
    ns_name_none = None
    with pytest.raises(Exception):
        marquez_client.get_namespace(ns_name_none)


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/'
    'test_get_datasources_with_limit_and_offset.yaml')
def test_get_datasources_with_limit_and_offset(
        marquez_client, existing_datasource):
    get_datasources_response = marquez_client.list_datasources(
        limit=25, offset=3)
    assert existing_datasource in get_datasources_response['datasources']


def test_get_namespaces_with_invalid_dictionary(marquez_client):
    with pytest.raises(Exception):
        marquez_client.get_request(
            marquez_client.marquez_host, "namespaces", 5, timeout_ms=10000.0)
