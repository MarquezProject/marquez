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


class SourceType(Enum):
    MYSQL = "MYSQL"
    POSTGRESQL = "POSTGRESQL"
    REDSHIFT = "REDSHIFT"
    SNOWFLAKE = "SNOWFLAKE"
    KAFKA = "KAFKA"
    REST = "REST"


class DatasetType(Enum):
    DB_TABLE = "DB_TABLE"
    STREAM = "STREAM"
    HTTP_ENDPOINT = "HTTP_ENDPOINT"


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


class DatasetFieldType(Enum):
    NUMBER = 1
    DECIMAL = 2
    NUMERIC = 3
    INT = 4
    INTEGER = 5
    BIGINT = 6
    SMALLINT = 7
    FLOAT = 8
    FLOAT4 = 9
    FLOAT8 = 10
    DOUBLE = 11
    REAL = 12
    VARCHAR = 13
    CHAR = 14
    CHARACTER = 15
    STRING = 16
    TEXT = 17
    BINARY = 18
    VARBINARY = 19
    BOOLEAN = 20
    DATE = 21
    DATETIME = 22
    TIME = 23
    TIMESTAMP = 24
    TIMESTAMP_LTZ = 25
    TIMESTAMP_NTZ = 26
    TIMESTAMP_TZ = 27
    VARIANT = 28
    OBJECT = 29
    ARRAY = 30
