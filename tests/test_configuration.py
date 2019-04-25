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

import pytest
from marquez import client
from marquez.client import Client
from pytest import fixture


@fixture(scope="function")
def clear_env():
    os.environ.clear()


def test_marquez_host_required(clear_env):
    with pytest.raises(ValueError):
        Client(port=8080)


def test_marquez_port_required(clear_env):
    with pytest.raises(ValueError):
        Client(host="localhost")


def test_default_namespace(clear_env):
    m = Client(host="localhost", port=8080)
    assert m.namespace == client._DEFAULT_NAMESPACE_NAME


def test_namespace_name_from_env(clear_env):
    os.environ["MARQUEZ_NAMESPACE_NAME"] = "ns_from_env"

    m = Client(host="localhost", port=8080)
    assert m.namespace == "ns_from_env"


def test_namespace_name_override(clear_env):
    os.environ["MARQUEZ_NAMESPACE_NAME"] = "ns_from_env"

    m = Client(host="localhost", namespace_name="from_constructor", port=8080)
    assert m.namespace == "from_constructor"


def test_set_client_namespace(clear_env):
    namespace_name = "someRandomNS"
    m = Client(host="localhost", port=8080, namespace_name=namespace_name)
    assert m.namespace == namespace_name


def test_host_override(clear_env):
    os.environ["MARQUEZ_HOST"] = "env_host"
    m = Client(host="specified_host", port=8080)
    assert m._api_base == "http://specified_host:8080/api/v1"


def test_host_from_env(clear_env):
    os.environ["MARQUEZ_HOST"] = "env_host"
    m = Client(port=8080)
    assert m._api_base == "http://env_host:8080/api/v1"


def test_port_override(clear_env):
    os.environ["MARQUEZ_PORT"] = "9000"
    m = Client(host="localhost", port=8080)
    assert m._api_base == "http://localhost:8080/api/v1"


def test_port_from_env(clear_env):
    os.environ["MARQUEZ_PORT"] = "9000"
    m = Client(host="localhost")
    assert m._api_base == "http://localhost:9000/api/v1"


def test_mixed_configuration(clear_env):
    os.environ["MARQUEZ_HOST"] = "localhost"
    m = Client(port=9000)
    assert m._api_base == "http://localhost:9000/api/v1"


def test_timeout_from_env(clear_env):
    os.environ["MARQUEZ_TIMEOUT_MS"] = "3000"
    m = Client(host="localhost", port=8080)
    assert m._timeout == 3


def test_default_timeout(clear_env):
    m = Client(host="localhost", port=8080)
    assert m._timeout == client._DEFAULT_TIMEOUT_MS / 1000.0


def test_timeout_override(clear_env):
    os.environ["MARQUEZ_TIMEOUT_MS"] = "3000"
    m = Client(host="localhost", port=8080, timeout_ms=7000)
    assert m._timeout == 7.0


def test_timeout_not_truncated(clear_env):
    os.environ["MARQUEZ_TIMEOUT_MS"] = "3500"
    m = Client(host="localhost", port=8080)
    assert m._timeout == 3.5
