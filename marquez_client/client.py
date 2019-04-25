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
import collections
import os
import urllib

import requests
from marquez_client.utils import (_compose_path, handle_response)

_API_PATH = "api/v1"
_DEFAULT_TIMEOUT_MS = 5000
_DEFAULT_NAMESPACE_NAME = "default"


class Client(object):
    def __init__(self, host=None, port=None,
                 namespace_name=None, timeout_ms=None):
        host = host or os.environ.get('MARQUEZ_HOST')
        port = (str(port) if port else port) or os.environ.get('MARQUEZ_PORT')
        timeout_input = (str(timeout_ms) if timeout_ms
                         else os.environ.get('MARQUEZ_TIMEOUT_MS'))

        self._namespace_name = (namespace_name
                                or os.environ.get('MARQUEZ_NAMESPACE_NAME')
                                or _DEFAULT_NAMESPACE_NAME)
        self._api_base = "http://{0}:{1}/{2}".format(
            host, port, _API_PATH)
        self._timeout = self._set_timeout(timeout_input)

        if not host or not port:
            msg = ("Please provide host & port or set the proper env "
                   "vars: MARQUEZ_HOST, MARQUEZ_PORT")
            raise ValueError(msg)

    @property
    def namespace(self):
        return self._namespace_name

    #  Namespaces API
    @handle_response
    def get_namespace(self, namespace_name):
        if not namespace_name:
            raise ValueError("Please provide a namespace")
        path = "namespaces/{0}"
        path_args = [namespace_name]
        return self.get_request(path, path_args)

    @handle_response
    def list_namespaces(self):
        path = "namespaces"
        path_args = []
        return self.get_request(path, path_args)

    @handle_response
    def create_namespace(self, name, owner,
                         description=None):
        path = "namespaces/{0}"
        path_args = [name]
        payload = {
            "owner": owner,
        }
        if description:
            payload['description'] = description
        return self.put_request(path, path_args, payload)

    #  Datasources API
    @handle_response
    def list_datasources(self, limit=None, offset=None):
        path = "datasources"
        path_args = []
        request_args = {}
        if limit:
            request_args['limit'] = limit
        if offset:
            request_args['offset'] = offset
        return self.get_request(
            path, path_args, request_args)

    @handle_response
    def get_datasource(self, urn):
        path = "datasources/{0}"
        path_args = [urn]
        return self.get_request(path, path_args)

    @handle_response
    def create_datasource(self, name, connection_url):
        path = "datasources"
        path_args = []
        payload = {
            "name": name,
            "connectionUrl": connection_url
        }
        return self.post_request(path, path_args, payload)

    #  Jobs API
    @handle_response
    def create_job(self, job_name, location, input_dataset_urns,
                   output_dataset_urns, namespace_name=None,
                   description=None):
        if not namespace_name:
            namespace_name = self._namespace_name

        path = "namespaces/{0}/jobs/{1}"
        path_args = [namespace_name, job_name]
        payload = {
            "inputDatasetUrns": input_dataset_urns,
            "outputDatasetUrns": output_dataset_urns,
            "location": location
        }

        if description:
            payload['description'] = description
        return self.put_request(path, path_args, payload)

    @handle_response
    def get_job(self, job_name, namespace_name=None):
        if not namespace_name:
            namespace_name = self._namespace_name

        path = "namespaces/{0}/jobs/{1}"
        path_args = [namespace_name, job_name]
        return self.get_request(path, path_args)

    @handle_response
    def list_jobs(self, namespace_name=None):
        if not namespace_name:
            namespace_name = self._namespace_name

        path = "namespaces/{0}/jobs/"
        path_args = [namespace_name]
        result = self.get_request(path, path_args)
        return result

    @handle_response
    def create_job_run(self, job_name, job_run_args, namespace_name=None,
                       nominal_start_time=None,
                       nominal_end_time=None):
        if not namespace_name:
            namespace_name = self._namespace_name

        path = "namespaces/{0}/jobs/{1}/runs"
        path_args = [namespace_name, job_name]
        payload = {
            "nominalStartTime": nominal_start_time,
            "nominalEndTime": nominal_end_time,
            "runArgs": job_run_args
        }
        return self.post_request(path, path_args, payload)

    @handle_response
    def get_job_run(self, job_run_id):
        path = "jobs/runs/{0}"
        path_args = [job_run_id]
        return self.get_request(path, path_args)

    def mark_job_run_as_running(self, job_run_id):
        return self._mark_job_run_as(
            job_run_id, "run")

    def mark_job_run_as_completed(self, job_run_id):
        return self._mark_job_run_as(
            job_run_id, "complete")

    def mark_job_run_as_failed(self, job_run_id):
        return self._mark_job_run_as(
            job_run_id, "fail")

    def mark_job_run_as_aborted(self, job_run_id):
        return self._mark_job_run_as(
            job_run_id, "abort")

    @handle_response
    def _mark_job_run_as(self, job_run_id, action):
        path = "jobs/runs/{0}/{1}".format(job_run_id, action)
        path_args = [job_run_id, action]
        payload = None
        return self.put_request(path, path_args, payload)

    #  Datasets API
    @handle_response
    def create_dataset(self, name, datasource_urn, namespace_name=None,
                       description=None):
        if not namespace_name:
            namespace_name = self._namespace_name

        path = "namespaces/{0}/datasets"
        path_args = [namespace_name]
        payload = {
            'name': name,
            'datasourceUrn': datasource_urn
        }

        if description:
            payload['description'] = description

        return self.post_request(path, path_args, payload)

    @handle_response
    def get_dataset(self, urn, namespace_name=None):
        path = "namespaces/{0}/datasets/{1}"
        if not namespace_name:
            namespace_name = self._namespace_name

        path_args = [namespace_name, urn]
        return self.get_request(path, path_args)

    @handle_response
    def list_datasets(self, namespace_name=None):
        path = "namespaces/{0}/datasets"
        if not namespace_name:
            namespace_name = self._namespace_name

        path_args = [namespace_name]
        return self.get_request(path, path_args)

    def put_request(self, path, path_args=None, args=None):
        path = _compose_path(path, path_args)
        full_path = "{0}/{1}".format(self._api_base, path)
        return requests.put(url=full_path, json=args, timeout=self._timeout)

    def post_request(self, path, path_args=None, payload=None):
        path = _compose_path(path, path_args)
        full_path = "{0}/{1}".format(self._api_base, path)

        return requests.post(
            url=full_path, json=payload, timeout=self._timeout)

    def get_request(self, path, path_args=None, request_args=None):
        if request_args and not isinstance(request_args, collections.Mapping):
            raise Exception("Request argument must be a dictionary")
        path = _compose_path(path, path_args)
        full_path = "{0}/{1}".format(self._api_base, path)
        if request_args:
            arg_str = urllib.parse.urlencode(request_args)
            full_path = "{0}?{1}".format(full_path, arg_str)
        return requests.get(full_path, timeout=self._timeout)

    @staticmethod
    def _set_timeout(t):
        timeout_milliseconds = (float(t) if (t and t.isnumeric())
                                else _DEFAULT_TIMEOUT_MS)
        return timeout_milliseconds / 1000.0
