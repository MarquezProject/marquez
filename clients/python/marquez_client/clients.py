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

from marquez_client import MarquezClient, MarquezWriteOnlyClient
from marquez_client.http_backend import HttpBackend
from marquez_client.file_backend import FileBackend
from marquez_client.log_backend import LogBackend
from marquez_client.utils import Utils
from marquez_client.constants import (
    DEFAULT_MARQUEZ_BACKEND,
    DEFAULT_MARQUEZ_URL,
    DEFAULT_MARQUEZ_FILE,
    DEFAULT_TIMEOUT_MS
)


# Marquez Clients
class Clients(object):
    @staticmethod
    def new_client():
        return MarquezClient(
            url=os.environ.get('MARQUEZ_URL', DEFAULT_MARQUEZ_URL),
            api_key=os.environ.get('MARQUEZ_API_KEY')
        )

    @staticmethod
    def new_write_only_client():
        return MarquezWriteOnlyClient(
            backend=Clients._backend_from_env(),
        )

    @staticmethod
    def _backend_from_env():
        backend = \
            os.environ.get('MARQUEZ_BACKEND', DEFAULT_MARQUEZ_BACKEND).upper()

        if backend == 'HTTP':
            url = os.environ.get('MARQUEZ_URL', DEFAULT_MARQUEZ_URL)
            api_key = os.environ.get('MARQUEZ_API_KEY')
            timeout = Utils.to_seconds(
                os.environ.get('MARQUEZ_TIMEOUT_MS', DEFAULT_TIMEOUT_MS))
            return HttpBackend(url, timeout, api_key)
        elif backend == 'FILE':
            file = os.environ.get('MARQUEZ_FILE', DEFAULT_MARQUEZ_FILE)
            return FileBackend(file)
        elif backend == 'LOG':
            return LogBackend()
