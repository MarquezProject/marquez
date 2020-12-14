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

log = logging.getLogger(__name__)


class MockIdMapping:

    mapping = None

    def __init__(self):
        log.debug("MockIdMapping.init()")
        self.mapping = {}

    def set(self, key, value):
        self.mapping[key] = value

    def pop(self, key, _session):
        return self.mapping.pop(key)
