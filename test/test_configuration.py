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

import os
from unittest.mock import MagicMock

import pytest
from marquez_client.marquez import MarquezClient
from pytest import fixture


@fixture(scope="function")
def clear_env():
    os.environ.clear()


def test_marquez_host_required(clear_env):
    with pytest.raises(ConnectionError):
        MarquezClient(port=8080)


def test_marquez_port_required(clear_env):
    with pytest.raises(ConnectionError):
        MarquezClient(host="localhost")


def test_set_namespace(clear_env):
    ns_name = "someRandomNamespace"
    m = MarquezClient(host="localhost", port=8080)
    m._create_namespace = MagicMock()
    m.set_namespace(ns_name)
    assert m.get_namespace() == ns_name


def test_host_override(clear_env):
    os.environ["MARQUEZ_HOST"] = "real_host"
    m = MarquezClient(host="fake_host", port=8080)
    assert m.host == "real_host"


def test_port_override(clear_env):
    os.environ["MARQUEZ_PORT"] = "5000"
    m = MarquezClient(host="localhost", port=8080)
    assert m.port == "5000"


def test_mixed_configuration(clear_env):
    os.environ["MARQUEZ_HOST"] = "localhost"
    m = MarquezClient(port=5000)
    assert m.port == "5000"
    assert m.host == "localhost"


def test_timeout_from_env(clear_env):
    os.environ["MARQUEZ_TIMEOUT"] = "3"
    m = MarquezClient(host="localhost", port=8080)
    assert m.timeout == 3


def test_default_timeout(clear_env):
    m = MarquezClient(host="localhost", port=8080)
    assert m.timeout == MarquezClient.DEFAULT_TIMEOUT_SEC


def test_timeout_override(clear_env):
    os.environ["MARQUEZ_TIMEOUT"] = "3"
    m = MarquezClient(host="localhost", port=8080, timeout=7)
    assert m.timeout == 3
