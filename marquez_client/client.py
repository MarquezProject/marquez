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
import os
import requests
import time
import uuid

from .models import DatasetType, SourceType, JobType
from marquez_client import errors
from marquez_client import log
from marquez_client.constants import (
    ENABLE_SSL, DEFAULT_HOST, DEFAULT_PORT, DEFAULT_TIMEOUT_MS
)
from marquez_client.version import VERSION
from six.moves.urllib.parse import quote

_API_PATH = 'api/v1'
_USER_AGENT = f'marquez-python/{VERSION}'
_HEADERS = {'User-Agent': _USER_AGENT}


# Marquez Client
class MarquezClient(object):
    def __init__(self, enable_ssl=False, host=None, port=None,
                 timeout_ms=None):
        enable_ssl = enable_ssl or os.environ.get('ENABLE_SSL', ENABLE_SSL)
        host = host or os.environ.get('MARQUEZ_HOST', DEFAULT_HOST)
        port = port or os.environ.get('MARQUEZ_PORT', DEFAULT_PORT)
        self._timeout = self._to_seconds(timeout_ms or os.environ.get(
            'MARQUEZ_TIMEOUT_MS', DEFAULT_TIMEOUT_MS)
        )

        protocol = 'http'
        if enable_ssl:
            protocol = 'https'

        self._api_base = f'{protocol}://{host}:{port}/{_API_PATH}'

        if not port or port == 80:
            self._api_base = f'{protocol}://{host}/{_API_PATH}'

        log.debug(self._api_base)

    # Namespace API
    def create_namespace(self, namespace_name, owner_name, description=None):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')
        MarquezClient._check_name_length(owner_name, 'owner_name')

        payload = {
            'ownerName': owner_name
        }

        if description:
            payload['description'] = description

        return self._put(
            self._url('/namespaces/{0}', namespace_name),
            payload=payload
        )

    def get_namespace(self, namespace_name):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')

        return self._get(self._url('/namespaces/{0}', namespace_name))

    def list_namespaces(self, limit=None, offset=None):
        return self._get(
            self._url('/namespaces'),
            params={
                'limit': limit,
                'offset': offset
            }
        )

    # Source API
    def create_source(self, source_name, source_type, connection_url,
                      description=None):
        MarquezClient._check_name_length(source_name, 'source_name')
        MarquezClient._is_instance_of(source_type, SourceType)

        MarquezClient._is_valid_connection_url(connection_url)

        payload = {
            'type': source_type.value,
            'connectionUrl': connection_url
        }

        if description:
            payload['description'] = description

        return self._put(self._url('/sources/{0}', source_name),
                         payload=payload)

    def get_source(self, source_name):
        MarquezClient._check_name_length(source_name, 'source_name')

        return self._get(self._url('/sources/{0}', source_name))

    def list_sources(self, limit=None, offset=None):
        return self._get(
            self._url('/sources'),
            params={
                'limit': limit,
                'offset': offset
            }
        )

    # Datasets API
    def create_dataset(self, namespace_name, dataset_name, dataset_type,
                       physical_name, source_name,
                       description=None, run_id=None,
                       schema_location=None,
                       fields=None, tags=None):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')
        MarquezClient._check_name_length(dataset_name, 'dataset_name')
        MarquezClient._is_instance_of(dataset_type, DatasetType)

        if dataset_type == DatasetType.STREAM:
            MarquezClient._is_none(schema_location, 'schema_location')

        MarquezClient._check_name_length(physical_name, 'physical_name')
        MarquezClient._check_name_length(source_name, 'source_name')

        payload = {
            'type': dataset_type.value,
            'physicalName': physical_name,
            'sourceName': source_name,
        }

        if description:
            payload['description'] = description

        if run_id:
            payload['runId'] = run_id

        if fields:
            payload['fields'] = fields

        if tags:
            payload['tags'] = tags

        if schema_location:
            payload['schemaLocation'] = schema_location

        return self._put(
            self._url('/namespaces/{0}/datasets/{1}', namespace_name,
                      dataset_name),
            payload=payload
        )

    def get_dataset(self, namespace_name, dataset_name):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')
        MarquezClient._check_name_length(dataset_name, 'dataset_name')

        return self._get(
            self._url('/namespaces/{0}/datasets/{1}',
                      namespace_name, dataset_name)
        )

    def list_datasets(self, namespace_name, limit=None, offset=None):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')

        return self._get(
            self._url('/namespaces/{0}/datasets', namespace_name),
            params={
                'limit': limit,
                'offset': offset
            }
        )

    def tag_dataset(self, namespace_name, dataset_name, tag_name):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')
        MarquezClient._check_name_length(dataset_name, 'dataset_name')

        if not tag_name:
            raise ValueError('tag_name must not be None')

        return self._post(
            self._url('/namespaces/{0}/datasets/{1}/tags/{2}',
                      namespace_name, dataset_name, tag_name)
        )

    def tag_dataset_field(self, namespace_name, dataset_name, field_name,
                          tag_name):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')
        MarquezClient._check_name_length(dataset_name, 'dataset_name')
        MarquezClient._check_name_length(field_name, 'field_name')
        MarquezClient._check_name_length(tag_name, 'tag_name')

        return self._post(
            self._url('/namespaces/{0}/datasets/{1}/fields/{2}/tags/{3}',
                      namespace_name, dataset_name, field_name, tag_name)
        )

    # Job API
    def create_job(self, namespace_name, job_name, job_type, location=None,
                   input_dataset=None,
                   output_dataset=None, description=None, context=None):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')
        MarquezClient._check_name_length(job_name, 'job_name')
        MarquezClient._is_instance_of(job_type, JobType)

        payload = {
            'inputs': input_dataset or [],
            'outputs': output_dataset or [],
            'type': job_type.name
        }

        if context:
            payload['context'] = context

        if location:
            payload['location'] = location

        if description:
            payload['description'] = description

        return self._put(
            self._url('/namespaces/{0}/jobs/{1}', namespace_name, job_name),
            payload=payload
        )

    def get_job(self, namespace_name, job_name):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')
        MarquezClient._check_name_length(job_name, 'job_name')

        return self._get(
            self._url('/namespaces/{0}/jobs/{1}', namespace_name, job_name)
        )

    def list_jobs(self, namespace_name, limit=None, offset=None):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')

        return self._get(
            self._url('/namespaces/{0}/jobs', namespace_name),
            params={
                'limit': limit,
                'offset': offset
            }
        )

    def create_job_run(self, namespace_name, job_name,
                       nominal_start_time=None,
                       nominal_end_time=None, run_args=None,
                       mark_as_running=False):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')
        MarquezClient._check_name_length(job_name, 'job_name')

        payload = {}

        if nominal_start_time:
            payload['nominalStartTime'] = nominal_start_time

        if nominal_end_time:
            payload['nominalEndTime'] = nominal_end_time

        if run_args:
            payload['runArgs'] = run_args

        response = self._post(
            self._url('/namespaces/{0}/jobs/{1}/runs',
                      namespace_name, job_name),
            payload=payload)

        if mark_as_running:
            run_id = response['runId']
            response = self.mark_job_run_as_started(run_id)

        return response

    def list_job_runs(self, namespace_name, job_name, limit=None,
                      offset=None):
        MarquezClient._check_name_length(namespace_name, 'namespace_name')
        MarquezClient._check_name_length(job_name, 'job_name')

        return self._get(
            self._url(
                '/namespaces/{0}/jobs/{1}/runs',
                namespace_name,
                job_name),
            params={
                'limit': limit,
                'offset': offset
            }
        )

    def get_job_run(self, run_id):
        self._is_valid_uuid(run_id, 'run_id')

        return self._get(self._url('/jobs/runs/{0}', run_id))

    def mark_job_run_as_started(self, run_id):
        return self.__mark_job_run_as(run_id, 'start')

    def mark_job_run_as_completed(self, run_id):
        return self.__mark_job_run_as(run_id, 'complete')

    def mark_job_run_as_failed(self, run_id):
        return self.__mark_job_run_as(run_id, 'fail')

    def mark_job_run_as_aborted(self, run_id):
        return self.__mark_job_run_as(run_id, 'abort')

    def list_tags(self, limit=None, offset=None):
        return self._get(
            self._url('/tags'),
            params={
                'limit': limit,
                'offset': offset
            }
        )

    def __mark_job_run_as(self, run_id, action):
        MarquezClient._is_valid_uuid(run_id, 'run_id')

        return self._post(
            self._url('/jobs/runs/{0}/{1}', run_id, action), payload={}
        )

    # Common
    def _url(self, path, *args):
        encoded_args = [quote(arg.encode('utf-8'), safe='') for arg in args]
        return f'{self._api_base}{path.format(*encoded_args)}'

    def _post(self, url, payload, as_json=True):
        now_ms = self._now_ms()

        response = requests.post(
            url=url, headers=_HEADERS, json=payload, timeout=self._timeout)
        log.info(f'{url}', method='POST', payload=json.dumps(
            payload), duration_ms=(self._now_ms() - now_ms))

        return self._response(response, as_json)

    def _put(self, url, payload=None, as_json=True):
        now_ms = self._now_ms()

        response = requests.put(
            url=url, headers=_HEADERS, json=payload, timeout=self._timeout)
        log.info(f'{url}', method='PUT', payload=json.dumps(
            payload), duration_ms=(self._now_ms() - now_ms))

        return self._response(response, as_json)

    def _get(self, url, params=None, as_json=True):
        now_ms = self._now_ms()

        response = requests.get(
            url, params=params, headers=_HEADERS, timeout=self._timeout)
        log.info(f'{url}', method='GET',
                 duration_ms=(self._now_ms() - now_ms))

        return self._response(response, as_json)

    @staticmethod
    def _now_ms():
        return int(round(time.time() * 1000))

    def _response(self, response, as_json):
        try:
            response.raise_for_status()
        except requests.exceptions.HTTPError as e:
            self._raise_api_error(e)

        return response.json() if as_json else response.text

    def _raise_api_error(self, e):
        # TODO: https://github.com/MarquezProject/marquez-python/issues/55
        raise errors.APIError() from e

    @staticmethod
    def _to_seconds(timeout_ms):
        return float(timeout_ms) / 1000.0

    @staticmethod
    def _is_none(variable_value, variable_name):
        if not variable_value:
            raise ValueError(f"{variable_name} must not be None")

    @staticmethod
    def _check_name_length(variable_value, variable_name):
        MarquezClient._is_none(variable_value, variable_name)

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
    def _is_valid_uuid(variable_value, variable_name):
        MarquezClient._is_none(variable_value, variable_name)

        try:
            uuid.UUID(str(variable_value))
        except ValueError:
            raise ValueError(f"{variable_name} must be a valid UUID")

    @staticmethod
    def _is_instance_of(variable_value, variable_enum_type):
        if not isinstance(variable_value, variable_enum_type):
            raise ValueError(f"{variable_value} must be an instance"
                             f" of {variable_enum_type}")

    @staticmethod
    def _is_valid_connection_url(connection_url):
        MarquezClient._is_none(connection_url, 'connection_url')
