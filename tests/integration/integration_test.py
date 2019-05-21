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

import subprocess
import sys

import pytest
import requests
from marquez_client import MarquezClient
from marquez_client.constants import NOT_FOUND
from requests import ReadTimeout
from urllib3.exceptions import MaxRetryError
from urllib3.util.retry import Retry

MARQUEZ_HOST = "marquez"
MARQUEZ_PORT = "5000"


@pytest.fixture
def broken_network():
    iptables_drop_packets(True)
    yield
    iptables_drop_packets(False)


@pytest.fixture(scope="module")
def wait_for_marquez():
    url = 'http://{}:{}/ping'.format(MARQUEZ_HOST, MARQUEZ_PORT)
    session = requests.Session()
    retry = Retry(total=5, backoff_factor=0.5)
    adapter = requests.adapters.HTTPAdapter(max_retries=retry)
    session.mount('http://', adapter)
    session.get(url)


def test_bad_host(wait_for_marquez):
    c = MarquezClient(host="bad-host", port=MARQUEZ_PORT)
    with pytest.raises(requests.exceptions.ConnectionError) as e:
        c.get_namespace("no_connection")
    assert isinstance(e.value.args[0], MaxRetryError)


def test_bad_port(wait_for_marquez):
    c = MarquezClient(host=MARQUEZ_HOST, port="6000")
    with pytest.raises(requests.exceptions.ConnectionError) as e:
        c.get_namespace("no_connection")
    assert isinstance(e.value.args[0], MaxRetryError)


def test_timeout(wait_for_marquez, broken_network):
    c = MarquezClient(host=MARQUEZ_HOST, port=MARQUEZ_PORT, timeout_ms=1)

    expected_namespace = "timeout_test"
    with pytest.raises(ReadTimeout):
        c.get_namespace(expected_namespace)


def test_namespace_not_found(wait_for_marquez):
    c = MarquezClient(host=MARQUEZ_HOST, port=MARQUEZ_PORT)

    expected_namespace = "not_found"
    assert c.get_namespace(expected_namespace) == NOT_FOUND


def iptables_drop_packets(drop):
    subprocess.run(["iptables",
                    "-A" if drop else "-D", "INPUT",
                    "-p", "tcp",
                    "--tcp-flags", "PSH", "PSH",
                    "--sport", MARQUEZ_PORT,
                    "-j", "DROP"])


if __name__ == "__main__":
    pytest.main([sys.argv[0]])
