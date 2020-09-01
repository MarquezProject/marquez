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
import marquez_client

from marquez_client.file_backend import FileBackend
from marquez_client.http_backend import HttpBackend
from marquez_client.constants import (DEFAULT_MARQUEZ_BACKEND,
                                      DEFAULT_MARQUEZ_URL,
                                      DEFAULT_MARQUEZ_FILE,
                                      DEFAULT_TIMEOUT_MS)
from marquez_client.utils import Utils

log = logging.getLogger(__name__)


# Marquez Clients
class Clients(object):
    def __init__(self):
        log.debug("Clients.init")

    @staticmethod
    def new_client():
        url = os.environ.get('MARQUEZ_URL', DEFAULT_MARQUEZ_URL)
        return marquez_client.MarquezClient(url)

    @staticmethod
    def new_write_only_client():
        return marquez_client.MarquezWriteOnlyClient(Clients.from_env())

    @staticmethod
    def from_env():
        backend_env = \
            os.environ.get('MARQUEZ_BACKEND', DEFAULT_MARQUEZ_BACKEND)

        if backend_env == 'http':
            url = os.environ.get('MARQUEZ_URL', DEFAULT_MARQUEZ_URL)
            timeout = Utils.to_seconds(
                os.environ.get('MARQUEZ_TIMEOUT_MS', DEFAULT_TIMEOUT_MS))
            return HttpBackend(url, timeout)
        elif backend_env == 'file':
            file = os.environ.get('MARQUEZ_FILE', DEFAULT_MARQUEZ_FILE)
            return FileBackend(file)
