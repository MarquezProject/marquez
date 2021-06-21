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
from unittest.mock import patch, MagicMock

import pytest
from marquez_client.utils import Utils
from marquez_client.models import DatasetType, JobType, RunState
import time


def test_mk_fields_from():
    fields_name_error = [{}]
    fields_type_error = [{"name": "flight_id"}]
    fields_valid = [
        {
            "name": "flight_id",
            "type": "integer",
            "description": "flight id",
            "tags": ["tag1", "tag2"]
        },
        {
            "name": "flight_name",
            "type": "VARCHAR",
            "description": "flight name",
            "tags": ["tag3", "tag4"]
        },
        {
            "name": "flight_date",
            "type": "timestamp",
            "description": "flight date"
        }
    ]
    new_fields_valid = [
        {
            "name": "flight_id",
            "type": "INTEGER",
            "description": "flight id",
            "tags": ["tag1", "tag2"]
        },
        {
            "name": "flight_name",
            "type": "VARCHAR",
            "description": "flight name",
            "tags": ["tag3", "tag4"]
        },
        {
            "name": "flight_date",
            "type": "TIMESTAMP",
            "description": "flight date"
        }
    ]
    assert Utils.mk_fields_from(fields=fields_valid) == new_fields_valid
    with pytest.raises(ValueError):
        Utils.mk_fields_from(fields=fields_name_error)
    with pytest.raises(ValueError):
        Utils.mk_fields_from(fields=fields_type_error)


def test_is_none():
    with pytest.raises(ValueError):
        Utils.is_none(None, None)


def test_check_name_length():
    with pytest.raises(ValueError):
        Utils.check_name_length(variable_value='a'*65,
                                variable_name='namespace_name')
    with pytest.raises(ValueError):
        Utils.check_name_length(variable_value='a'*65,
                                variable_name='owner_name')
    with pytest.raises(ValueError):
        Utils.check_name_length(variable_value='a'*65,
                                variable_name='source_name')
    with pytest.raises(ValueError):
        Utils.check_name_length(variable_value='a'*256,
                                variable_name='dataset_name')
    with pytest.raises(ValueError):
        Utils.check_name_length(variable_value='a'*256,
                                variable_name='field_name')
    with pytest.raises(ValueError):
        Utils.check_name_length(variable_value='a'*256,
                                variable_name='job_name')
    with pytest.raises(ValueError):
        Utils.check_name_length(variable_value='a'*256,
                                variable_name='tag_name')


def test_is_valid_uuid():
    with pytest.raises(ValueError):
        Utils.is_valid_uuid(variable_value='not-uuid',
                            variable_name='var_name')


def test_is_instance_of():
    with pytest.raises(ValueError):
        Utils.is_instance_of(variable_value=JobType.BATCH,
                             variable_enum_type=DatasetType)
    with pytest.raises(ValueError):
        Utils.is_instance_of(
            variable_value=DatasetType.DB_TABLE, variable_enum_type=JobType)
    with pytest.raises(ValueError):
        Utils.is_instance_of(variable_value=JobType.BATCH,
                             variable_enum_type=RunState)


@patch("time.time", MagicMock(return_value=1500100900))
def test_now_ms():
    assert int(round(time.time() * 1000)) == Utils.now_ms()
