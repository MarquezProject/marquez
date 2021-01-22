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

import logging.config
import os
import unittest

from marquez_client import Clients
from marquez_client.log_backend import LogBackend

_NAMESPACE = "my-namespace"
log = logging.getLogger(__name__)


class TestMarquezWriteOnlyClientLog(unittest.TestCase):
    def test_log_backend(self):
        log.debug("MarquezWriteOnlyClient.setup(): ")
        os.environ['MARQUEZ_BACKEND'] = 'log'
        self.client_wo_log = Clients.new_write_only_client()
        log.info("created marquez_client_wo_log.")
        if not isinstance(self.client_wo_log._backend, LogBackend):
            raise Exception("Not a LogBackend.")


if __name__ == '__main__':
    unittest.main()
