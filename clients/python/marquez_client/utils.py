# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

import json
import uuid
from datetime import datetime

import pytz
import time
from pyrfc3339 import generate


class Utils:
    @staticmethod
    def mk_fields_from(fields):
        # TODO(wslulciuc) We don't need to copy the fields. This method
        # can be removed when we generate the client models from the
        # Marquez openAPI spec
        for field in fields:
            if 'name' not in field:
                raise ValueError('field name must not be None')
        return fields

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

    @staticmethod
    def get_json(file):
        with open(file) as json_file:
            return json.load(json_file)

    @staticmethod
    def add_auth_to(headers, api_key):
        headers['Authorization'] = f"Bearer {api_key}"
