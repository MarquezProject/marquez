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
            'owner': owner_name
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

    def create_job(self, job_name, location, input_dataset_urns=None,
                   output_dataset_urns=None, description=None,
                   namespace_name=None):
        if not job_name:
            raise ValueError('job_name must not be None')
        if not location:
            raise ValueError('location must not be None')

        if not namespace_name:
            namespace_name = self._namespace_name

        payload = {
            'inputDatasetUrns': input_dataset_urns or [],
            'outputDatasetUrns': output_dataset_urns or [],
            'location': location
        }

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
            response = self.mark_job_run_as_running(run_id)

        return response

    def get_job_run(self, run_id):
        if not run_id:
            raise ValueError('run_id must not be None')

        return self._get(self._url('/jobs/runs/{0}', run_id))

    def mark_job_run_as_running(self, run_id):
        return self._mark_job_run_as(run_id, 'run')

    def mark_job_run_as_completed(self, run_id):
        return self._mark_job_run_as(run_id, 'complete')

    def mark_job_run_as_failed(self, run_id):
        return self._mark_job_run_as(run_id, 'fail')

    def mark_job_run_as_aborted(self, run_id):
        return self._mark_job_run_as(run_id, 'abort')

    def _mark_job_run_as(self, run_id, action):
        if not run_id:
            raise ValueError('run_id must not be None')

        return self._put(
            self._url('/jobs/runs/{0}/{1}', run_id, action), as_json=False
        )

    def create_datasource(self, datasource_name, connection_url):
        if not datasource_name:
            raise ValueError('datasource_name must not be None')
        if not connection_url:
            raise ValueError('connection_url must not be None')

        return self._post(
            self._url('/datasources'),
            payload={
                'name': datasource_name,
                'connectionUrl': connection_url
            }
        )

    def get_datasource(self, datasource_urn):
        if not datasource_urn:
            raise ValueError('datasource_urn must not be None')

        return self._get(self._url('/datasources/{0}', datasource_urn))

    def list_datasources(self, limit=None, offset=None):
        return self._get(
            self._url('/datasources'),
            params={
                'limit': limit,
                'offset': offset
            }
        )

    def create_dataset(self, dataset_name, datasource_urn,
                       description=None, namespace_name=None):
        if not dataset_name:
            raise ValueError('dataset_name must not be None')
        if not datasource_urn:
            raise ValueError('datasource_urn must not be None')

        if not namespace_name:
            namespace_name = self._namespace_name

        payload = {
            'name': dataset_name,
            'datasourceUrn': datasource_urn
        }

        if description:
            payload['description'] = description

        return self._post(
            self._url('/namespaces/{0}/datasets', namespace_name),
            payload=payload
        )

    def get_dataset(self, dataset_urn, namespace_name=None):
        if not dataset_urn:
            raise ValueError('dataset_urn must not be None')

        if not namespace_name:
            namespace_name = self._namespace_name

        return self._get(
            self._url('/namespaces/{0}/datasets/{1}',
                      namespace_name, dataset_urn)
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
