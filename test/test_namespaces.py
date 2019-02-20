import os

from pytest import fixture
import pytest
import vcr

from marquez_client.marquez import MarquezClient


@fixture(scope='class')
def set_valid_env():
    os.environ[MarquezClient.MARQUEZ_HOST_KEY] = "localhost"
    os.environ[MarquezClient.MARQUEZ_PORT_KEY] = "8080"


@fixture(scope='class')
def marquez_client(set_valid_env):
    return MarquezClient()


@vcr.use_cassette('fixtures/vcr/test_namespaces/test_create_namespace.yaml')
def test_create_namespace(marquez_client):
    ns_name = "ns_name_997"
    owner_name = "some_owner"
    description = "this is a very nice namespace."

    marquez_client.set_namespace(ns_name, owner_name, description)
    client_ns = marquez_client.get_namespace()
    assert_str = "mismatch between created namespace and ns set"
    assert client_ns == ns_name, assert_str


def test_namespace_not_set(set_valid_env):
    m = MarquezClient()
    with pytest.raises(Exception):
        m.create_job('some_job', 'some_location',
                     ['input1', 'input2'],
                     ['output1', 'output2'])


if __name__ == "__main__":
    pytest.main(["./test_namespaces.py", "-s"])
