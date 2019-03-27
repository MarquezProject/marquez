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
from marquez_codegen_client import CreateDatasource

from marquez_client.marquez import MarquezClient
from pytest import fixture


@fixture(scope='class')
def marquez_client():
    return MarquezClient(host="localhost",
                         port=8080)


@fixture(scope='class')
@vcr.use_cassette(
    'test/fixtures/vcr/test_datasources/datasource_for_datasource_tests.yaml')
def existing_datasource(marquez_client):
    datasource_name = "financials_db200"
    datasource_url = "jdbc:postgresql://localhost:5431/reporting_system"
    create_datasource_request = CreateDatasource(
        datasource_name, datasource_url)
    return marquez_client.create_datasource(create_datasource_request)


@vcr.use_cassette(
    'test/fixtures/vcr/test_datasources/test_create_datasource.yaml')
def test_create_datasource(marquez_client):
    datasource_name = "financials_db201"
    datasource_url = "jdbc:postgresql://localhost:5431/reporting_system"
    create_datasource_request = CreateDatasource(
        datasource_name, datasource_url)
    datasource_response = marquez_client.create_datasource(
        create_datasource_request)
    assert datasource_response.name == datasource_name
    assert datasource_response.connection_url == datasource_url
    assert 'created_at' in datasource_response.attribute_map


@vcr.use_cassette(
    'test/fixtures/vcr/test_datasources/test_get_datasource.yaml')
def test_get_datasource(marquez_client, existing_datasource):
    datasource_response = marquez_client.get_datasource(
        existing_datasource.urn)
    assert existing_datasource.name == datasource_response.name
    assert existing_datasource.urn == datasource_response.urn
    response_url = datasource_response.connection_url
    assert response_url == existing_datasource.connection_url
    assert datasource_response.created_at == existing_datasource.created_at


@vcr.use_cassette(
    'test/fixtures/vcr/test_datasources/test_get_datasources.yaml')
def test_get_datasources(marquez_client, existing_datasource):
    get_datasources_response = marquez_client.get_all_datasources()
    assert existing_datasource in get_datasources_response._datasources
