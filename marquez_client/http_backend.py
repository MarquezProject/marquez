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

import requests

from marquez_client import errors
from marquez_client.backend import Backend

log = logging.getLogger(__name__)


class HttpBackend(Backend):
    def __init__(self, url, timeout):
        self._timeout = timeout
        self._url = url

    def put(self, path, headers, json):
        log.debug("_put()")

        response = requests.put(
            url=f'{self._url}{path}', headers=headers, json=json,
            timeout=self._timeout)

        return self._response(response, as_json=True)

    def post(self, path, headers, json=None):
        log.debug("_post()")

        response = requests.post(
            url=f'{self._url}{path}', headers=headers, json=json,
            timeout=self._timeout)

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
