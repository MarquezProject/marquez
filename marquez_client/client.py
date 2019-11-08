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

from .models import DatasetType, SourceType
from marquez_client import errors
from marquez_client import log
from marquez_client.constants import (
    DEFAULT_HOST, DEFAULT_PORT, DEFAULT_TIMEOUT_MS, DEFAULT_NAMESPACE_NAME
)
from marquez_client.version import VERSION
from six.moves.urllib.parse import quote

_API_PATH = 'api/v1'

_USER_AGENT = f'marquez-python/{VERSION}'
_HEADERS = {'User-Agent': _USER_AGENT}


class MarquezClient(object):
    def __init__(self, host=None, port=None,
                 timeout_ms=None, namespace_name=None):
        host = host or os.environ.get('MARQUEZ_HOST', DEFAULT_HOST)
        port = port or os.environ.get('MARQUEZ_PORT', DEFAULT_PORT)
        self._timeout = self._to_seconds(timeout_ms or os.environ.get(
            'MARQUEZ_TIMEOUT_MS', DEFAULT_TIMEOUT_MS)
        )
        self._namespace_name = namespace_name or os.environ.get(
            'MARQUEZ_NAMESPACE', DEFAULT_NAMESPACE_NAME
        )
        self._api_base = f'http://{host}:{port}/{_API_PATH}'

    @property
    def namespace(self):
        return self._namespace_name

    def create_namespace(self, namespace_name, owner_name, description=None):
        if not namespace_name:
            raise ValueError('namespace_name must not be None')
        if not owner_name:
            raise ValueError('owner_name must not be None')

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
        if not namespace_name:
            raise ValueError('namespace_name must not be None')

        return self._get(self._url('/namespaces/{0}', namespace_name))

    def list_namespaces(self, limit=None, offset=None):
        return self._get(
            self._url('/namespaces'),
            params={
                'limit': limit,
                'offset': offset
            }
        )

    def create_job(self, job_name, job_type, location=None, input_dataset=None,
                   output_dataset=None, description=None, context=None,
                   namespace_name=None):
        if not job_name:
            raise ValueError('job_name must not be None')
        if not job_type:
            raise ValueError('job_type must not be None')

        if not namespace_name:
            namespace_name = self._namespace_name

        payload = {
            'inputs': input_dataset or [],
            'outputs': output_dataset or [],
            'type': job_type
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

    def get_job(self, job_name, namespace_name=None):
        if not job_name:
            raise ValueError('job_name must not be None')

        if not namespace_name:
            namespace_name = self._namespace_name

        return self._get(
            self._url('/namespaces/{0}/jobs/{1}', namespace_name, job_name)
        )

    def list_jobs(self, limit=None, offset=None, namespace_name=None):
        if not namespace_name:
            namespace_name = self._namespace_name

        return self._get(
            self._url('/namespaces/{0}/jobs', namespace_name),
            params={
                'limit': limit,
                'offset': offset
            }
        )

    def create_job_run(self, job_name, nominal_start_time=None,
                       nominal_end_time=None, run_args=None,
                       mark_as_running=False, namespace_name=None):
        if not job_name:
            raise ValueError('job_name must not be None')

        if not namespace_name:
            namespace_name = self._namespace_name

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

    def get_job_run(self, run_id):
        if not run_id:
            raise ValueError('run_id must not be None')

        return self._get(self._url('/jobs/runs/{0}', run_id))

    def list_job_runs(self, job_name, limit=None,
                      offset=None, namespace_name=None):
        if not namespace_name:
            namespace_name = self._namespace_name

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

    def mark_job_run_as_started(self, run_id):
        return self._mark_job_run_as(run_id, 'start')

    def mark_job_run_as_completed(self, run_id):
        return self._mark_job_run_as(run_id, 'complete')

    def mark_job_run_as_failed(self, run_id):
        return self._mark_job_run_as(run_id, 'fail')

    def mark_job_run_as_aborted(self, run_id):
        return self._mark_job_run_as(run_id, 'abort')

    def _mark_job_run_as(self, run_id, action):
        if not run_id:
            raise ValueError('run_id must not be None')

        return self._post(
            self._url('/jobs/runs/{0}/{1}', run_id, action), payload={}
        )

    # Sources API
    def create_source(self, source_name, source_type, connection_url,
                      description=None):
        if not source_name:
            raise ValueError('source_name must not be None')
        if not connection_url:
            raise ValueError('connection_url must not be None')
        if not isinstance(source_type, SourceType):
            raise ValueError(f'source_type must be instance of SourceType')

        payload = {'type': source_type.value,
                   'connectionUrl': connection_url}

        if description:
            payload['description'] = description

        return self._put(self._url('/sources/{0}', source_name),
                         payload=payload)

    def get_source(self, source_name):
        if not source_name:
            raise ValueError('source_name must not be None')

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

    def create_dataset(self, dataset_name, dataset_type,
                       physical_name, source_name,
                       description=None, run_id=None,
                       schema_location=None,
                       namespace_name=None):
        if not dataset_name:
            raise ValueError('dataset_name must not be None')
        if not isinstance(dataset_type, DatasetType):
            raise ValueError('dataset_type must be instance of DatasetType')
        if not physical_name:
            raise ValueError('physical_name must not be None')
        if not source_name:
            raise ValueError('source_name must not be None')

        if dataset_type == DatasetType.STREAM and not schema_location:
            raise ValueError('STREAM type datasets must have schema_location')

        if not namespace_name:
            namespace_name = self._namespace_name

        payload = {
            'type': dataset_type.value,
            'physicalName': physical_name,
            'sourceName': source_name,
        }

        if description:
            payload['description'] = description

        if run_id:
            payload['runId'] = run_id

        if schema_location:
            payload['schemaLocation'] = schema_location

        return self._put(
            self._url('/namespaces/{0}/datasets/{1}', namespace_name,
                      dataset_name),
            payload=payload
        )

    def get_dataset(self, dataset_name, namespace_name=None):
        if not dataset_name:
            raise ValueError('dataset_name must not be None')

        if not namespace_name:
            namespace_name = self._namespace_name

        return self._get(
            self._url('/namespaces/{0}/datasets/{1}',
                      namespace_name, dataset_name)
        )

    def list_datasets(self, namespace_name=None, limit=None, offset=None):
        if not namespace_name:
            namespace_name = self._namespace_name

        return self._get(
            self._url('/namespaces/{0}/datasets', namespace_name),
            params={
                'limit': limit,
                'offset': offset
            }
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
