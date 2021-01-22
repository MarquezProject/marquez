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

from marquez_airflow.utils import execute_git

log = logging.getLogger(__name__)


def execute_git_mock(*args, **kwargs):
    # just mock the git revision
    log.debug("execute_git_mock()")
    if len(args) == 2 and len(args[1]) > 0 and args[1][0] == 'rev-list':
        return 'abcd1234'
    return execute_git(*args, **kwargs)
