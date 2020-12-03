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

from enum import Enum


class DatasetId:
    def __init__(self, namespace: str, name: str):
        self.namespace = namespace
        self.name = name


class JobId:
    def __init__(self, namespace: str, name: str):
        self.namespace = namespace
        self.name = name


class DatasetType(Enum):
    DB_TABLE = "DB_TABLE"
    STREAM = "STREAM"


class JobType(Enum):
    BATCH = "BATCH"
    STREAM = "STREAM"
    SERVICE = "SERVICE"


class RunState(Enum):
    NEW = 'NEW'
    RUNNING = 'RUNNING'
    COMPLETED = 'COMPLETED'
    FAILED = 'FAILED'
    ABORTED = 'ABORTED'
