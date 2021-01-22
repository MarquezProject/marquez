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
import unittest

import mock

from marquez_client.log_backend import LogBackend
from marquez_client.utils import Utils

log = logging.getLogger(__name__)


class TestLogBackend(unittest.TestCase):
    def setUp(self):
        log.debug("TestLogBackend.setup(): ")
        self.log_backend = LogBackend()
        self.put_json = Utils.get_json("tests/resources/put.json")
        self.post_json = Utils.get_json("tests/resources/post.json")

    @mock.patch("json.dumps")
    def test_put(self, mock_put):
        log.debug("TestLogBackend.test_put(): ")

        path = self.put_json["path"]
        headers = self.put_json["headers"]
        json_payload = self.put_json["payload"]

        self.log_backend.put(path, headers, json)
        mock_put.return_value = self.put_json

        assert path == mock_put.return_value["path"]
        assert headers == mock_put.return_value["headers"]
        assert json_payload == mock_put.return_value["payload"]

    @mock.patch("json.dumps")
    def test_post(self, mock_post):
        log.debug("TestLogBackend.test_post(): ")

        path = self.post_json["path"]
        headers = self.post_json["headers"]
        json_payload = self.post_json["payload"]

        self.log_backend.post(path, headers, json)
        mock_post.return_value = self.post_json

        assert path == mock_post.return_value["path"]
        assert headers == mock_post.return_value["headers"]
        assert json_payload == mock_post.return_value["payload"]


if __name__ == '__main__':
    unittest.main()
