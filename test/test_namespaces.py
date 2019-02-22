import os

import pytest
import vcr
from marquez_client.marquez import MarquezClient
from pytest import fixture


@fixture(scope='class')
def set_valid_env():
    os.environ[MarquezClient.MARQUEZ_HOST_KEY] = "localhost"
    os.environ[MarquezClient.MARQUEZ_PORT_KEY] = "8080"


@fixture(scope='class')
def marquez_client(set_valid_env):
    return MarquezClient()


@fixture(scope='function')
@vcr.use_cassette('fixtures/vcr/test_job/namespace_for_namespace_tests.yaml')
def namespace(marquez_client):
    ns_name = "ns_for_namespace_test_1"
    owner_name = "ns_owner"
    description = "this is a namespace for testing."

    return marquez_client._create_namespace(ns_name, owner_name, description)


@vcr.use_cassette('fixtures/vcr/test_namespaces/test_create_namespace.yaml')
def test_create_namespace(marquez_client):
    ns_name = "ns_name_997"
    owner_name = "some_owner"
    description = "this is a very nice namespace."

    marquez_client.set_namespace(ns_name, owner_name, description)
    client_ns = marquez_client.get_namespace()
    assert_str = "mismatch between created namespace and ns set"
    assert client_ns == ns_name, assert_str


@vcr.use_cassette('fixtures/vcr/test_namespaces/test_get_namespace.yaml')
def test_get_namespace(marquez_client, namespace):
    returned_ns = marquez_client.get_namespace_info(namespace.name)
    assert returned_ns.name == namespace.name
    assert returned_ns.description == namespace.description
    assert returned_ns.owner == namespace.owner


def test_get_namespace_invalid_input(marquez_client):
    ns_name_none = None
    with pytest.raises(Exception):
        marquez_client.get_namespace_info(ns_name_none)


def test_namespace_not_set(set_valid_env):
    m = MarquezClient()
    with pytest.raises(Exception):
        m.create_job('some_job', 'some_location',
                     ['input1', 'input2'],
                     ['output1', 'output2'])


if __name__ == "__main__":
    pytest.main(["./test_namespaces.py", "-s"])
