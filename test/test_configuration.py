import os
from unittest.mock import MagicMock, patch

import pytest
from pytest import fixture

from marquez_client.marquez import MarquezClient


@fixture(scope='function')
def set_valid_env():
    os.environ[MarquezClient.MARQUEZ_HOST_KEY] = "localhost"
    os.environ[MarquezClient.MARQUEZ_PORT_KEY] = "8080"


def test_marquez_valid_configuration(set_valid_env):
    MarquezClient()


def test_marquez_host_required(set_valid_env):
    del os.environ[MarquezClient.MARQUEZ_HOST_KEY]
    with pytest.raises(Exception):
        MarquezClient()


def test_marquez_port_required(set_valid_env):
    del os.environ[MarquezClient.MARQUEZ_PORT_KEY]
    with pytest.raises(Exception):
        MarquezClient()


def test_set_namespace(set_valid_env):
    ns_name = 'someRandomNamespace'

    m = MarquezClient()
    m._create_namespace = MagicMock()
    m.set_namespace(ns_name)

    assert m.get_namespace() == ns_name


def test_namespace_not_set(set_valid_env):
    m = MarquezClient()
    with pytest.raises(Exception):
        m.create_job('some_job', 'some_location', ['input1', 'input2'], ['output1', 'output2'])


if __name__ == "__main__":
    pytest.main()
