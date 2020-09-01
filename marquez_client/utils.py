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
import uuid
import pytz
import time

from datetime import datetime
from pyrfc3339 import generate
from marquez_client.models import DatasetFieldType, DatasetType

log = logging.getLogger(__name__)


class Utils(object):
    def make_field(name, data_type, description=None):
        if isinstance(data_type, str):
            if not DatasetFieldType.__members__.__contains__(data_type):
                raise ValueError(f'Invalid field type: {data_type}')
        elif isinstance(data_type, DatasetFieldType):
            data_type = data_type.name
        else:
            raise ValueError('data_type must be a str or a DatasetFieldType')

        DatasetType.__members__.get(data_type)
        field = {
            'name': name,
            'type': data_type
        }
        if description:
            field['description'] = description
        return field

    @staticmethod
    def to_seconds(timeout_ms):
        return float(timeout_ms) / 1000.0

    @staticmethod
    def is_none(variable_value, variable_name):
        if not variable_value:
            raise ValueError(f"{variable_name} must not be None")

    @staticmethod
    def check_name_length(variable_value, variable_name):
        Utils.is_none(variable_value, variable_name)

        # ['namespace_name', 'owner_name', 'source_name'] <= 64
        # ['dataset_name', 'field_name', 'job_name', 'tag_name'] <= 255
        if variable_name in ['namespace_name', 'owner_name', 'source_name']:
            if len(variable_value) > 64:
                raise ValueError(f"{variable_name} length is"
                                 f" {len(variable_value)}, must be <= 64")
        else:
            if len(variable_value) > 255:
                raise ValueError(f"{variable_name} length is"
                                 f" {len(variable_value)}, must be <= 255")

    @staticmethod
    def is_valid_uuid(variable_value, variable_name):
        Utils.is_none(variable_value, variable_name)

        try:
            uuid.UUID(str(variable_value))
        except ValueError:
            raise ValueError(f"{variable_name} must be a valid UUID")

    @staticmethod
    def is_instance_of(variable_value, variable_enum_type):
        if not isinstance(variable_value, variable_enum_type):
            raise ValueError(f"{variable_value} must be an instance"
                             f" of {variable_enum_type}")

    @staticmethod
    def is_valid_connection_url(connection_url):
        Utils.is_none(connection_url, 'connection_url')

    @staticmethod
    def now_ms():
        return int(round(time.time() * 1000))

    @staticmethod
    def utc_now():
        return str(generate(datetime.utcnow().replace(tzinfo=pytz.utc),
                            microseconds=True))
