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

from marquez_client import MarquezClient
from marquez_client.constants import (
    DEFAULT_HOST, DEFAULT_PORT, DEFAULT_TIMEOUT_MS, DEFAULT_NAMESPACE_NAME
)
from pytest import fixture


@fixture(scope='function')
def clear_env():
    os.environ.clear()


def test_host_default(clear_env):
    client = MarquezClient()
    assert client._api_base == f'http://{DEFAULT_HOST}:8080/api/v1'


def test_host_from_env(clear_env):
    os.environ['MARQUEZ_HOST'] = 'marquez.dev'

    client = MarquezClient()
    assert client._api_base == f'http://marquez.dev:8080/api/v1'


def test_host_from_constructor(clear_env):
    os.environ['MARQUEZ_HOST'] = 'marquez.dev'

    client = MarquezClient(host='marquez.staging')
    assert client._api_base == f'http://marquez.staging:8080/api/v1'


def test_port_default(clear_env):
    client = MarquezClient()
    assert client._api_base == f'http://{DEFAULT_HOST}:{DEFAULT_PORT}/api/v1'


def test_port_from_env(clear_env):
    os.environ['MARQUEZ_PORT'] = '5000'

    client = MarquezClient()
    assert client._api_base == f'http://{DEFAULT_HOST}:5000/api/v1'


def test_port_from_constructor(clear_env):
    os.environ['MARQUEZ_PORT'] = '5000'

    client = MarquezClient(port=5001)
    assert client._api_base == f'http://{DEFAULT_HOST}:5001/api/v1'


def test_timeout_default(clear_env):
    client = MarquezClient()
    assert client._timeout == DEFAULT_TIMEOUT_MS / 1000.0


def test_timeout_from_env(clear_env):
    os.environ['MARQUEZ_TIMEOUT_MS'] = '2000'

    client = MarquezClient()
    assert client._timeout == 2.0


def test_timeout_from_constructor(clear_env):
    os.environ['MARQUEZ_TIMEOUT_MS'] = '2000'

    client = MarquezClient(timeout_ms=3500)
    assert client._timeout == 3.5


def test_namespace_default(clear_env):
    client = MarquezClient()
    assert client.namespace == DEFAULT_NAMESPACE_NAME


def test_namespace_from_env(clear_env):

    os.environ['MARQUEZ_NAMESPACE'] = 'from_env'

    client = MarquezClient()
    assert client.namespace == 'from_env'

    # TODO: https://github.com/MarquezProject/marquez-python/issues/59
    os.environ.clear()


def test_namespace_from_constructor(clear_env):
    os.environ['MARQUEZ_NAMESPACE'] = 'from_env'

    client = MarquezClient(namespace_name='from_constructor')
    assert client.namespace == 'from_constructor'

    # TODO: https://github.com/MarquezProject/marquez-python/issues/59
    os.environ.clear()
