# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

import json
import logging
import os
import requests

import marquez_client

from deprecation import deprecated
from six.moves.urllib.parse import quote

from marquez_client import errors
from marquez_client.constants import (
    DEFAULT_TIMEOUT_MS,
    DEFAULT_LIMIT,
    DEFAULT_OFFSET,
    API_PATH_V1
)
from marquez_client.models import (
    DatasetId,
    DatasetType,
    JobType
)
from marquez_client.utils import Utils

_USER_AGENT = f'marquez-python/{marquez_client.__version__}'
_HEADERS = {'User-Agent': _USER_AGENT}

log = logging.getLogger(__name__)


class MarquezClient:
    def __init__(self, url, timeout_ms=None, api_key: str = None):
        self._timeout = Utils.to_seconds(timeout_ms or os.environ.get(
            'MARQUEZ_TIMEOUT_MS', DEFAULT_TIMEOUT_MS)
        )
        self._api_base = f"{url}{API_PATH_V1}"

        if api_key:
            Utils.add_auth_to(_HEADERS, api_key)

    def create_namespace(self, namespace_name, owner_name, description=None):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(owner_name, 'owner_name')

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
        Utils.check_name_length(namespace_name, 'namespace_name')

        return self._get(self._url('/namespaces/{0}', namespace_name))

    def list_namespaces(self, limit=None, offset=None):
        return self._get(
            self._url('/namespaces'),
            params={
                'limit': limit or DEFAULT_LIMIT,
                'offset': offset or DEFAULT_OFFSET
            }
        )

    @deprecated(deprecated_in='0.20.0', removed_in='0.25.0',
                details='Use OpenLineage instead, see `https://openlineage.io`')
    def create_source(self, source_name, source_type, connection_url,
                      description=None):
        Utils.check_name_length(source_name, 'source_name')

        Utils.is_valid_connection_url(connection_url)

        payload = {
            'type': source_type.upper(),
            'connectionUrl': connection_url
        }

        if description:
            payload['description'] = description

        return self._put(self._url('/sources/{0}', source_name),
                         payload=payload)

    def get_source(self, source_name):
        Utils.check_name_length(source_name, 'source_name')

        return self._get(self._url('/sources/{0}', source_name))

    def list_sources(self, limit=None, offset=None):
        return self._get(
            self._url('/sources'),
            params={
                'limit': limit or DEFAULT_LIMIT,
                'offset': offset or DEFAULT_OFFSET
            }
        )

    @deprecated(deprecated_in='0.20.0', removed_in='0.25.0',
                details='Use OpenLineage instead, see `https://openlineage.io`')
    def create_dataset(self, namespace_name, dataset_name, dataset_type,
                       dataset_physical_name, source_name,
                       description=None, run_id=None,
                       schema_location=None,
                       fields=None, tags=None):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(dataset_name, 'dataset_name')
        Utils.is_instance_of(dataset_type, DatasetType)

        if dataset_type == DatasetType.STREAM:
            Utils.is_none(schema_location, 'schema_location')

        Utils.check_name_length(dataset_physical_name, 'dataset_physical_name')
        Utils.check_name_length(source_name, 'source_name')

        payload = {
            'type': dataset_type.value,
            'physicalName': dataset_physical_name,
            'sourceName': source_name,
        }

        if description:
            payload['description'] = description

        if run_id:
            payload['runId'] = run_id

        if fields:
            payload['fields'] = Utils.mk_fields_from(fields)

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
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(dataset_name, 'dataset_name')

        return self._get(
            self._url('/namespaces/{0}/datasets/{1}',
                      namespace_name, dataset_name)
        )

    def get_dataset_version(self, namespace_name, dataset_name, version):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(dataset_name, 'dataset_name')

        if not version:
            raise ValueError('version must not be None')

        return self._get(
            self._url('/namespaces/{0}/datasets/{1}/versions/{2}',
                      namespace_name, dataset_name, version)
        )

    def list_dataset_versions(self, namespace_name, dataset_name,
                              limit=None, offset=None):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(dataset_name, 'dataset_name')

        return self._get(
            self._url('/namespaces/{0}/datasets/{1}/versions',
                      namespace_name, dataset_name),
            params={
                'limit': limit or DEFAULT_LIMIT,
                'offset': offset or DEFAULT_OFFSET
            }
        )

    def list_datasets(self, namespace_name, limit=None, offset=None):
        Utils.check_name_length(namespace_name, 'namespace_name')

        return self._get(
            self._url('/namespaces/{0}/datasets', namespace_name),
            params={
                'limit': limit or DEFAULT_LIMIT,
                'offset': offset or DEFAULT_OFFSET
            }
        )

    def tag_dataset(self, namespace_name, dataset_name, tag_name):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(dataset_name, 'dataset_name')

        if not tag_name:
            raise ValueError('tag_name must not be None')

        return self._post(
            self._url('/namespaces/{0}/datasets/{1}/tags/{2}',
                      namespace_name, dataset_name, tag_name)
        )

    def tag_dataset_field(self, namespace_name, dataset_name,
                          dataset_field_name, tag_name):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(dataset_name, 'dataset_name')
        Utils.check_name_length(dataset_field_name, 'dataset_field_name')
        Utils.check_name_length(tag_name, 'tag_name')

        return self._post(
            self._url('/namespaces/{0}/datasets/{1}/fields/{2}/tags/{3}',
                      namespace_name, dataset_name, dataset_field_name,
                      tag_name)
        )

    @deprecated(deprecated_in='0.20.0', removed_in='0.25.0',
                details='Use OpenLineage instead, see `https://openlineage.io`')
    def create_job(self, namespace_name, job_name, job_type, location=None,
                   inputs: [DatasetId] = None, outputs: [DatasetId] = None,
                   description=None, context=None, run_id=None):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(job_name, 'job_name')
        Utils.is_instance_of(job_type, JobType)

        payload = {
            'type': job_type.value,
            'inputs': [
                input.__dict__ for input in inputs
            ] if inputs else [],
            'outputs': [
                output.__dict__ for output in outputs
            ] if outputs else []
        }

        if run_id:
            payload['runId'] = run_id

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
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(job_name, 'job_name')

        return self._get(
            self._url('/namespaces/{0}/jobs/{1}', namespace_name, job_name)
        )

    def list_jobs(self, namespace_name, limit=None, offset=None):
        Utils.check_name_length(namespace_name, 'namespace_name')

        return self._get(
            self._url('/namespaces/{0}/jobs', namespace_name),
            params={
                'limit': limit or DEFAULT_LIMIT,
                'offset': offset or DEFAULT_OFFSET
            }
        )

    @deprecated(deprecated_in='0.20.0', removed_in='0.25.0',
                details='Use OpenLineage instead, see `https://openlineage.io`')
    def create_job_run(self, namespace_name, job_name, run_id=None,
                       nominal_start_time=None,
                       nominal_end_time=None, run_args=None,
                       mark_as_running=False):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(job_name, 'job_name')

        payload = {}

        if run_id:
            payload['id'] = run_id

        if nominal_start_time:
            payload['nominalStartTime'] = nominal_start_time

        if nominal_end_time:
            payload['nominalEndTime'] = nominal_end_time

        if run_args:
            payload['args'] = run_args

        response = self._post(
            self._url('/namespaces/{0}/jobs/{1}/runs',
                      namespace_name, job_name),
            payload=payload)

        if mark_as_running:
            response = self.mark_job_run_as_started(response['id'])

        return response

    def list_job_runs(self, namespace_name, job_name, limit=None,
                      offset=None):
        Utils.check_name_length(namespace_name, 'namespace_name')
        Utils.check_name_length(job_name, 'job_name')

        return self._get(
            self._url(
                '/namespaces/{0}/jobs/{1}/runs',
                namespace_name,
                job_name),
            params={
                'limit': limit or DEFAULT_LIMIT,
                'offset': offset or DEFAULT_OFFSET
            }
        )

    def get_job_run(self, run_id):
        Utils.is_valid_uuid(run_id, 'run_id')
        return self._get(self._url('/jobs/runs/{0}', run_id))

    @deprecated(deprecated_in='0.20.0', removed_in='0.25.0',
                details='Use OpenLineage instead, see `https://openlineage.io`')
    def mark_job_run_as_started(self, run_id, at=None):
        return self.__mark_job_run_as(run_id, 'start', at)

    @deprecated(deprecated_in='0.20.0', removed_in='0.25.0',
                details='Use OpenLineage instead, see `https://openlineage.io`')
    def mark_job_run_as_completed(self, run_id, at=None):
        return self.__mark_job_run_as(run_id, 'complete', at)

    @deprecated(deprecated_in='0.20.0', removed_in='0.25.0',
                details='Use OpenLineage instead, see `https://openlineage.io`')
    def mark_job_run_as_failed(self, run_id, at=None):
        return self.__mark_job_run_as(run_id, 'fail', at)

    @deprecated(deprecated_in='0.20.0', removed_in='0.25.0',
                details='Use OpenLineage instead, see `https://openlineage.io`')
    def mark_job_run_as_aborted(self, run_id, at=None):
        return self.__mark_job_run_as(run_id, 'abort', at)

    def list_tags(self, limit=None, offset=None):
        return self._get(
            self._url('/tags'),
            params={
                'limit': limit,
                'offset': offset
            }
        )

    def create_tag(self, name, description=None):
        payload = {
            'description': description
        }

        return self._put(
            self._url('/tags/{0}', name),
            payload=payload
        )

    @deprecated(deprecated_in='0.20.0', removed_in='0.25.0',
                details='Use OpenLineage instead, see `https://openlineage.io`')
    def __mark_job_run_as(self, run_id, action, at=None):
        Utils.is_valid_uuid(run_id, 'run_id')

        return self._post(
            self._url('/jobs/runs/{0}/{1}?at={2}', run_id, action,
                      at if at else Utils.utc_now())
        )

    def _url(self, path, *args):
        encoded_args = [quote(arg.encode('utf-8'), safe='') for arg in args]
        return f'{self._api_base}{path.format(*encoded_args)}'

    def _post(self, url, payload=None, as_json=True):
        now_ms = Utils.now_ms()

        response = requests.post(
            url=url, headers=_HEADERS, json=payload, timeout=self._timeout
        )
        log.info(
            f"{url} method=POST payload={json.dumps(payload)} "
            f"duration_ms={Utils.now_ms() - now_ms}"
        )

        return self._response(response, as_json)

    def _put(self, url, payload=None, as_json=True):
        now_ms = Utils.now_ms()

        response = requests.put(
            url=url, headers=_HEADERS, json=payload, timeout=self._timeout
        )
        log.info(
            f"{url} method=PUT payload={json.dumps(payload)} "
            f"duration_ms={Utils.now_ms() - now_ms}"
        )

        return self._response(response, as_json)

    def _get(self, url, params=None, as_json=True):
        now_ms = Utils.now_ms()

        response = requests.get(
            url=url, params=params, headers=_HEADERS, timeout=self._timeout
        )
        log.info(
            f"{url} method=GET duration_ms={Utils.now_ms() - now_ms}"
        )

        return self._response(response, as_json)

    def _response(self, response, as_json):
        try:
            response.raise_for_status()
        except requests.exceptions.HTTPError as e:
            self._raise_api_error(e)

        return response.json() if as_json else response.text

    def _raise_api_error(self, e):
        # TODO: https://github.com/MarquezProject/marquez-python/issues/55
        raise errors.APIError() from e
