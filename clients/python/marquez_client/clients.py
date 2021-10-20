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

from marquez_client import MarquezClient
from marquez_client.constants import (
    DEFAULT_MARQUEZ_URL
)


# Marquez Clients
class Clients(object):
    @staticmethod
    def new_client():
        return MarquezClient(
            url=os.environ.get('MARQUEZ_URL', DEFAULT_MARQUEZ_URL),
            api_key=os.environ.get('MARQUEZ_API_KEY')
        )
