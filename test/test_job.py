import os

import pytest
import vcr
from marquez_client.marquez import MarquezClient
from pytest import fixture


@fixture(scope='function')
def set_valid_env():
    os.environ[MarquezClient.MARQUEZ_HOST_KEY] = "localhost"
    os.environ[MarquezClient.MARQUEZ_PORT_KEY] = "8080"


@fixture(scope='function')
def marquez_client(set_valid_env):
    return MarquezClient()


@fixture(scope='function')
@vcr.use_cassette('fixtures/vcr/test_job/namespace_for_job.yaml')
def namespace(marquez_client):
    ns_name = "ns_for_job_test_1"
    owner_name = "some_owner"
    description = "this is a very nice namespace."

    marquez_client.set_namespace(ns_name, owner_name, description)
    client_ns = marquez_client.get_namespace()
    return client_ns


@vcr.use_cassette('fixtures/vcr/test_job/test_create_job.yaml')
def test_create_job(marquez_client, namespace):
    marquez_client.set_namespace(namespace)
    created_job = marquez_client.create_job(
        'some_job', 'some_location',
        ['input1', 'input2'],
        ['output1', 'output2'])

    assert created_job.location == 'some_location'
    assert created_job.name == "some_job"


def test_namespace_not_set(set_valid_env):
    m = MarquezClient()
    with pytest.raises(Exception):
        m.create_job(
            'some_job', 'some_location',
            ['input1', 'input2'],
            ['output1', 'output2'])


if __name__ == "__main__":
    pytest.main(["./test_job.py", "-s"])
