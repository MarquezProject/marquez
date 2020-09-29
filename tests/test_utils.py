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

import logging
import os
from unittest import mock

from airflow.models import Connection
from marquez_airflow.utils import get_connection_uri

log = logging.getLogger(__name__)

CONN_ID = 'test_db'
CONN_URI = 'postgres://localhost:5432/testdb'


@mock.patch("marquez_airflow.utils._get_connection")
def test_get_connection_uri(mock_get_connection):
    log.debug("test_get_connection_url()")
    mock_get_connection.return_value = Connection(
        conn_id=CONN_ID,
        uri=CONN_URI
    )
    assert get_connection_uri(CONN_ID) == CONN_URI


def test_get_connection_uri_from_env():
    # Set the environment variable as AIRFLOW_CONN_<conn_id>
    os.environ[f"AIRFLOW_CONN_{CONN_ID.upper()}"] = CONN_URI
    assert get_connection_uri(CONN_ID) == CONN_URI
