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

import json
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

    def put(self, path, headers, payload):
        log.debug("_put()")

        put_details = {}

        put_details['method'] = 'PUT'
        put_details['path'] = path
        put_details['headers'] = headers
        put_details['payload'] = payload

        put_json = json.dumps(put_details)
        log.info(put_json)

        self._sync_file(put_json)

    def post(self, path, headers, payload=None):
        log.debug("_post()")

        post_details = {}

        post_details['method'] = 'POST'
        post_details['path'] = path
        post_details['headers'] = headers
        if payload:
            post_details['payload'] = payload

        post_json = json.dumps(post_details)
        log.info(post_json)

        self._sync_file(post_json)

    def _sync_file(self, json_data):
        self._file.write(json_data)
        self._file.write(os.linesep)
        self._file.flush()
        os.fsync(self._file.fileno())

    def __del__(self):
        self._file.close()
