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

from deprecation import deprecated

from marquez_client import MarquezClient
from marquez_client.utils import Utils
from marquez_client.constants import (
    DEFAULT_MARQUEZ_URL,
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
    @deprecated(deprecated_in="0.17.0",
                details="Prefer `Clients.new_client()`. This method is scheduled to be removed "
                        "in release `0.19.0`.")
    def new_write_only_client():
        return MarquezWriteOnlyClient(
            backend=Clients._backend_from_env(),
        )
