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

from marquez_client.backend import Backend

log = logging.getLogger(__name__)


class LogBackend(Backend):
    def __init__(self):
        log.debug("LogBackend.init")

    def put(self, path, headers, payload):
        log.debug("_put()")

        put_details = {}

        put_details['method'] = 'PUT'
        put_details['path'] = path
        put_details['headers'] = headers
        put_details['payload'] = payload

        log_details = json.dumps(put_details)
        log.info(log_details)

    def post(self, path, headers, payload=None):
        log.debug("_post()")

        post_details = {}

        post_details['method'] = 'POST'
        post_details['path'] = path
        post_details['headers'] = headers
        if payload:
            post_details['payload'] = payload

        log_details = json.dumps(post_details)
        log.info(log_details)
