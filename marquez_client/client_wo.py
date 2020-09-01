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

import datetime
import logging

from six.moves.urllib.parse import quote

from marquez_client.version import VERSION
from .models import DatasetType, SourceType, JobType
from .utils import Utils

_API_PATH = '/api/v1'
_USER_AGENT = f'marquez-python/{VERSION}'
_HEADERS = {'User-Agent': _USER_AGENT}

log = logging.getLogger(__name__)


# Marquez Write Only Client
class MarquezWriteOnlyClient(object):
    def __init__(self, backend):
        self._backend = backend

    # Namespace API
    def create_namespace(self, namespace_name, owner_name, description=None):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(owner_name, 'owner_name')

        payload = {
            'ownerName': owner_name
        }

        if description:
            payload['description'] = description

        return self._backend.put(
            self.path('/namespaces/{0}', namespace_name),
            headers=_HEADERS,
            json=payload
        )

    # Source API
    def create_source(self, source_name, source_type, connection_url,
                      description=None):
        Utils.check_name_length(source_name, 'source_name')
        Utils.is_instance_of(source_type, SourceType)

        Utils.is_valid_connection_url(connection_url)

        payload = {
            'type': source_type.value,
            'connectionUrl': connection_url
        }

        if description:
            payload['description'] = description

        return self._backend.put(
            self.path('/sources/{0}', source_name),
            headers=_HEADERS,
            json=payload)

    # Datasets API
    def create_dataset(self, namespace_name, dataset_name, dataset_type,
                       physical_name, source_name, run_id,
                       description=None, schema_location=None,
                       fields=None, tags=None):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(dataset_name, 'dataset_name')
        Utils.is_instance_of(dataset_type, DatasetType)

        if dataset_type == DatasetType.STREAM:
            Utils.is_none(schema_location, 'schema_location')

        Utils.is_none(run_id, 'run_id')
        Utils.check_name_length(physical_name, 'physical_name')
        Utils.check_name_length(source_name, 'source_name')

        payload = {
            'type': dataset_type.value,
            'physicalName': physical_name,
            'sourceName': source_name,
            'runId': run_id,
        }

        if description:
            payload['description'] = description

        if fields:
            payload['fields'] = fields

        if tags:
            payload['tags'] = tags

        if schema_location:
            payload['schemaLocation'] = schema_location

        return self._backend.put(
            self.path('/namespaces/{0}/datasets/{1}', namespace_name,
                      dataset_name),
            headers=_HEADERS,
            json=payload
        )

    # Job API
    def create_job(self, namespace_name, job_name, job_type,
                   location=None, input_dataset=None,
                   output_dataset=None, description=None, context=None):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(job_name, 'job_name')
        Utils.is_instance_of(job_type, JobType)

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

        return self._backend.put(
            self.path('/namespaces/{0}/jobs/{1}', namespace_name, job_name),
            headers=_HEADERS,
            json=payload
        )

    def create_job_run(self, namespace_name, job_name, run_id,
                       nominal_start_time=None,
                       nominal_end_time=None, run_args=None,
                       mark_as_running=False):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(job_name, 'job_name')

        payload = {}

        payload['id'] = run_id

        if nominal_start_time:
            payload['nominalStartTime'] = nominal_start_time

        if nominal_end_time:
            payload['nominalEndTime'] = nominal_end_time

        if run_args:
            payload['runArgs'] = run_args

        response = self._backend.post(
            self.path('/namespaces/{0}/jobs/{1}/runs',
                      namespace_name, job_name),
            headers=_HEADERS,
            json=payload)

        if mark_as_running:
            response = self.mark_job_run_as_started(
                run_id, str(datetime.datetime.utcnow()))

        return response

    def mark_job_run_as_started(self, run_id, action_at=None):
        return self.__mark_job_run_as(run_id, 'start', action_at)

    def mark_job_run_as_completed(self, run_id, action_at=None):
        return self.__mark_job_run_as(run_id, 'complete', action_at)

    def mark_job_run_as_failed(self, run_id, action_at=None):
        return self.__mark_job_run_as(run_id, 'fail', action_at)

    def mark_job_run_as_aborted(self, run_id, action_at=None):
        return self.__mark_job_run_as(run_id, 'abort', action_at)

    def __mark_job_run_as(self, run_id, action, action_at=None):
        Utils.is_valid_uuid(run_id, 'run_id')

        return self._backend.post(
            self.path('/jobs/runs/{0}/{1}?at={2}', run_id, action,
                      action_at if action_at else Utils.utc_now()),
            headers=_HEADERS,
            json={}
        )

    # Common
    @staticmethod
    def path(self, path, *args):
        encoded_args = [quote(arg.encode('utf-8'), safe='') for arg in args]
        return f'{_API_PATH}{path.format(*encoded_args)}'
