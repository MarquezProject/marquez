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
from marquez_client.constants import NOT_FOUND
from marquez_client.utils import InvalidRequestError
from pytest import fixture


@fixture(scope='class')
def namespace_name():
    return "namespace_for_datasets_tests_3"


@fixture(scope='class')
def marquez_client_default_ns():
    return Client(host="localhost",
                  port=8080)


@fixture(scope='class')
def marquez_client(namespace):
    return Client(host="localhost", namespace_name=namespace['name'],
                  port=8080)


@fixture(scope='class')
@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasets/namespace_for_datasets.yaml')
def namespace(marquez_client_default_ns, namespace_name):
    owner_name = "some_owner"
    description = "this is a very nice namespace."

    client_ns = marquez_client_default_ns.create_namespace(
        namespace_name, owner_name, description)
    return client_ns


@fixture(scope='class')
@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasets/datasource_for_datasets_tests.yaml')
def existing_datasource(marquez_client):
    datasource_name = "financials_db505"
    datasource_url = "jdbc:redshift://localhost:5431/reporting_system"
    return marquez_client.create_datasource(datasource_name, datasource_url)


@fixture(scope='class')
@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasets/dataset_for_datasets_test.yaml')
def existing_dataset(marquez_client, existing_datasource):
    dataset_name = 'dataset_fixture_3'
    dataset_description = 'a dataset for testing'

    result = marquez_client.create_dataset(
        name=dataset_name,
        datasource_urn=existing_datasource['urn'],
        description=dataset_description)
    return result


@fixture(scope='class')
@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasets/'
    'dataset_default_ns_for_datasets_test.yaml')
def existing_dataset_default_ns(
        marquez_client_default_ns, existing_datasource):
    dataset_name = 'dataset_fixture_for_default_ns'
    dataset_description = 'a dataset for testing'

    return marquez_client_default_ns.create_dataset(
        name=dataset_name,
        datasource_urn=existing_datasource['urn'],
        description=dataset_description)


@vcr.use_cassette('tests/fixtures/vcr/test_datasets/test_create_dataset.yaml')
def test_create_dataset(marquez_client, existing_datasource):
    dataset_name = 'some_dataset_999'
    description = "someDescription"
    created_dataset = marquez_client.create_dataset(
        dataset_name, existing_datasource['urn'], description=description)

    assert existing_datasource['name'] in str(created_dataset['urn'])
    assert created_dataset['name'] == dataset_name
    assert created_dataset['description'] == description


@pytest.mark.skip("Disabled until Marquez issue 458 is resolved")
@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasources/'
    'test_create_datasource_special_chars.yaml')
def test_create_datasource_special_chars(marquez_client, existing_datasource):
    dataset_name = "financi@ls db20!"
    with pytest.raises(InvalidRequestError):
        marquez_client.create_dataset(
            dataset_name, existing_datasource['urn'])


@vcr.use_cassette('tests/fixtures/vcr/test_datasets/test_get_dataset.yaml')
def test_get_dataset(marquez_client, existing_dataset):
    retrieved_dataset = marquez_client.get_dataset(existing_dataset['urn'])

    assert retrieved_dataset['description'] == existing_dataset['description']
    assert retrieved_dataset['name'] == existing_dataset['name']
    assert 'createdAt' in retrieved_dataset
    assert retrieved_dataset['createdAt'] is not None


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasets/test_get_dataset_specify_ns.yaml')
def test_get_dataset_specify_ns(
        marquez_client_default_ns, namespace_name, existing_dataset):
    retrieved_dataset = marquez_client_default_ns.get_dataset(
        existing_dataset['urn'], namespace_name=namespace_name)

    assert retrieved_dataset['description'] == existing_dataset['description']
    assert retrieved_dataset['name'] == existing_dataset['name']
    assert 'createdAt' in retrieved_dataset
    assert retrieved_dataset['createdAt'] is not None


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasets/test_get_dataset_default_ns.yaml')
def test_get_dataset_default_ns(
        marquez_client_default_ns, existing_dataset_default_ns):
    retrieved_dataset = marquez_client_default_ns.get_dataset(
        urn=existing_dataset_default_ns['urn'])

    assert (retrieved_dataset['description'] ==
            existing_dataset_default_ns['description'])
    assert retrieved_dataset['name'] == existing_dataset_default_ns['name']
    assert 'createdAt' in retrieved_dataset
    assert retrieved_dataset['createdAt'] is not None


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasets/test_get_dataset_malformed_urn.yaml')
def test_get_dataset_malformed_urn(marquez_client):
    assert NOT_FOUND == marquez_client.get_dataset("not_a_valid_urn")
    assert NOT_FOUND == marquez_client.get_dataset("*55;34/098## *!! x;;$")


@vcr.use_cassette('tests/fixtures/vcr/test_datasets/test_list_datsets.yaml')
def test_list_datasets(marquez_client, existing_dataset):
    retrieved_datasets = marquez_client.list_datasets()
    assert existing_dataset in retrieved_datasets['datasets']


@vcr.use_cassette(
    'tests/fixtures/vcr/test_datasets/test_list_datsets_default_ns.yaml')
def test_list_datasets_default_ns(
        marquez_client_default_ns, existing_dataset_default_ns):
    retrieved_datasets = marquez_client_default_ns.list_datasets()
    assert existing_dataset_default_ns in retrieved_datasets['datasets']
