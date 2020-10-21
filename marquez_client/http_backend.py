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

import requests

from marquez_client import errors
from marquez_client.backend import Backend
from marquez_client.utils import Utils
from marquez_client.constants import API_PATH_V1


class HttpBackend(Backend):
    def __init__(self, url, timeout, api_key: str = None):
        self._timeout = timeout
        self._api_base = f"{url}{API_PATH_V1}"
        self._api_key = api_key

    def put(self, path, headers, payload):
        if self._api_key:
            Utils.add_auth_to(headers, self._api_key)

        response = requests.put(
            url=f"{self._api_base}{path}",
            headers=headers,
            json=payload,
            timeout=self._timeout
        )

        return self._response(response, as_json=True)

    def post(self, path, headers, payload=None):
        if self._api_key:
            Utils.add_auth_to(headers, self._api_key)

        response = requests.post(
            url=f"{self._api_base}{path}",
            headers=headers,
            json=payload,
            timeout=self._timeout
        )

        return self._response(response, as_json=True)

    def _response(self, response, as_json):
        try:
            response.raise_for_status()
        except requests.exceptions.HTTPError as e:
            self._raise_api_error(e)

        return response.json() if as_json else response.text

    def _raise_api_error(self, e):
        # TODO: https://github.com/MarquezProject/marquez-python/issues/55
        raise errors.APIError() from e
