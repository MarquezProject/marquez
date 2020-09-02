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

from marquez_client.backend import Backend

log = logging.getLogger(__name__)


class FileBackend(Backend):
    def __init__(self, file):
        self._file = open(self._mkdir_if_not_exists(file), 'a')

    @staticmethod
    def _mkdir_if_not_exists(file):
        path, _ = os.path.split(file)
        os.makedirs(path, exist_ok=True)
        return file

    def put(self, path, headers, json):
        log.debug("_put()")

        put_details = {}

        put_details['method'] = 'PUT'
        put_details['path'] = path
        put_details['headers'] = headers
        put_details['payload'] = json

        log.info(put_details)

        self._file.write(f'{put_details}{os.linesep}')

    def post(self, path, headers, json=None):
        log.debug("_post()")

        post_details = {}

        post_details['method'] = 'POST'
        post_details['path'] = path
        post_details['headers'] = headers
        post_details['payload'] = json

        log.info(post_details)

        self._file.write(f'{post_details}{os.linesep}')

    def __del__(self):
        self._file.close()
